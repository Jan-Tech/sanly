package com.sanly.pension.service;

import com.sanly.pension.client.BridgePublisherService;
import com.sanly.pension.client.NotificationClient;
import com.sanly.pension.config.UserDetailsImpl;
import com.sanly.pension.dto.request.ApplyRetirementRequest;
import com.sanly.pension.dto.request.RejectRetirementRequest;
import com.sanly.pension.dto.response.EligibilityResponse;
import com.sanly.pension.dto.response.RetirementApplicationResponse;
import com.sanly.pension.entity.*;
import com.sanly.pension.exception.BusinessException;
import com.sanly.pension.exception.ResourceNotFoundException;
import com.sanly.pension.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RetirementService {
    private final RetirementApplicationRepository repo;
    private final PensionAccountRepository accountRepo;
    private final ContributionRecordRepository contributionRepo;
    private final PensionPaymentRepository paymentRepo;
    private final EligibilityCalculationService eligibilityCalc;
    private final BridgePublisherService bridgePublisher;
    private final NotificationClient notificationClient;

    @Transactional
    public RetirementApplicationResponse apply(ApplyRetirementRequest req) {
        PensionAccount account = accountRepo.findByCitizenNationalId(req.getCitizenNationalId())
                .orElseThrow(() -> new ResourceNotFoundException("No pension account for: " + req.getCitizenNationalId()));
        if (repo.existsByAccountCodeAndStatus(account.getAccountCode(), ApplicationStatus.PENDING))
            throw new BusinessException("A pending retirement application already exists");

        long monthsContributed = contributionRepo.countByAccountCodeAndStatus(
                account.getAccountCode(), ContributionStatus.VERIFIED);
        BigDecimal total = PensionAccountService.parseBigDecimal(account.getTotalContributions());
        EligibilityResponse elig = eligibilityCalc.calculate(account, monthsContributed, total);

        if (!elig.eligible())
            throw new BusinessException("Not yet eligible — eligibleAt: " + elig.eligibleAt()
                    + ", yearsRemaining: " + elig.yearsRemaining());

        RetirementApplication app = repo.save(RetirementApplication.builder()
                .accountCode(account.getAccountCode()).citizenNationalId(req.getCitizenNationalId())
                .appliedAt(LocalDateTime.now()).requestedStartDate(LocalDate.parse(req.getRequestedStartDate()))
                .status(ApplicationStatus.PENDING).build());
        return toResponse(app);
    }

    @Transactional(readOnly = true)
    public RetirementApplicationResponse findById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<RetirementApplicationResponse> findByNin(String nationalId) {
        return repo.findByCitizenNationalIdOrderByAppliedAtDesc(nationalId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RetirementApplicationResponse> findByStatus(ApplicationStatus status) {
        return repo.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Transactional
    public RetirementApplicationResponse approve(Long applicationId) {
        RetirementApplication app = findOrThrow(applicationId);
        if (app.getStatus() != ApplicationStatus.PENDING)
            throw new BusinessException("Application is not PENDING");

        PensionAccount account = accountRepo.findByAccountCode(app.getAccountCode()).orElseThrow();
        long monthsContributed = contributionRepo.countByAccountCodeAndStatus(
                account.getAccountCode(), ContributionStatus.VERIFIED);
        BigDecimal total = PensionAccountService.parseBigDecimal(account.getTotalContributions());
        EligibilityResponse elig = eligibilityCalc.calculate(account, monthsContributed, total);

        String monthlyAmount = elig.projectedMonthlyAmount().toPlainString();
        app.setStatus(ApplicationStatus.APPROVED);
        app.setProcessedByOfficerId(authenticatedOfficerId());
        app.setProcessedAt(LocalDateTime.now());
        app.setMonthlyPensionAmount(monthlyAmount);
        repo.save(app);

        account.setStatus(AccountStatus.PAYING);
        accountRepo.save(account);

        String payMonth = LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue());
        if (!paymentRepo.existsByAccountCodeAndPaymentMonth(account.getAccountCode(), payMonth)) {
            paymentRepo.save(PensionPayment.builder()
                    .accountCode(account.getAccountCode()).citizenNationalId(account.getCitizenNationalId())
                    .paymentMonth(payMonth).amount(monthlyAmount).status(PaymentStatus.SCHEDULED)
                    .scheduledDate(app.getRequestedStartDate()).build());
        }

        bridgePublisher.publishPensionStatus(account, monthlyAmount, (int) monthsContributed);
        notificationClient.send(account.getCitizenNationalId(), "PENSION_APPROVED", "EN", Map.of(
                "amount", monthlyAmount, "startDate", app.getRequestedStartDate().toString()));

        return toResponse(app);
    }

    @Transactional
    public RetirementApplicationResponse reject(Long applicationId, RejectRetirementRequest req) {
        RetirementApplication app = findOrThrow(applicationId);
        if (app.getStatus() != ApplicationStatus.PENDING)
            throw new BusinessException("Application is not PENDING");
        app.setStatus(ApplicationStatus.REJECTED);
        app.setRejectionReason(req.getRejectionReason());
        app.setProcessedByOfficerId(authenticatedOfficerId());
        app.setProcessedAt(LocalDateTime.now());
        return toResponse(repo.save(app));
    }

    private RetirementApplication findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
    }

    private Long authenticatedOfficerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetailsImpl) auth.getPrincipal()).getOfficerId();
    }

    private RetirementApplicationResponse toResponse(RetirementApplication a) {
        return new RetirementApplicationResponse(a.getApplicationId(), a.getAccountCode(),
                a.getCitizenNationalId(), a.getAppliedAt(), a.getRequestedStartDate(),
                a.getStatus().name(), a.getProcessedByOfficerId(), a.getProcessedAt(),
                a.getRejectionReason(), a.getMonthlyPensionAmount());
    }
}
