package com.sanly.pension.service;

import com.sanly.pension.client.CitizenRegistryClient;
import com.sanly.pension.client.NotificationClient;
import com.sanly.pension.dto.request.OpenAccountRequest;
import com.sanly.pension.dto.response.EligibilityResponse;
import com.sanly.pension.dto.response.PensionAccountResponse;
import com.sanly.pension.entity.*;
import com.sanly.pension.exception.BusinessException;
import com.sanly.pension.exception.ResourceNotFoundException;
import com.sanly.pension.repository.ContributionRecordRepository;
import com.sanly.pension.repository.PensionAccountRepository;
import com.sanly.pension.repository.PensionPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PensionAccountService {
    private final PensionAccountRepository repo;
    private final ContributionRecordRepository contributionRepo;
    private final PensionPaymentRepository paymentRepo;
    private final CodeGeneratorService codeGen;
    private final CitizenRegistryClient citizenRegistryClient;
    private final EligibilityCalculationService eligibilityCalc;
    private final NotificationClient notificationClient;

    @Transactional
    public PensionAccountResponse open(OpenAccountRequest req) {
        if (repo.findByCitizenNationalId(req.getCitizenNationalId()).isPresent())
            throw new BusinessException("Pension account already exists for: " + req.getCitizenNationalId());

        LocalDate birthDate = null;
        try { birthDate = citizenRegistryClient.verifyAndGetBirthDate(req.getCitizenNationalId()); }
        catch (Exception ignored) {}

        if (birthDate == null && req.getBirthDate() != null)
            birthDate = LocalDate.parse(req.getBirthDate());
        if (birthDate == null)
            throw new BusinessException("Could not determine birth date — provide birthDate in request");

        LocalDate employmentStart = LocalDate.parse(req.getEmploymentStartDate());
        String gender = EligibilityCalculationService.deriveGender(req.getCitizenNationalId());
        int retirementAge = EligibilityCalculationService.retirementAge(gender);
        LocalDate eligibleAt = EligibilityCalculationService.calculateEligibleAt(birthDate, gender, employmentStart);
        String code = codeGen.nextAccountCode();

        PensionAccount account = repo.save(PensionAccount.builder()
                .accountCode(code).citizenNationalId(req.getCitizenNationalId())
                .openedAt(LocalDateTime.now()).employmentStartDate(employmentStart)
                .birthDate(birthDate).gender(gender).retirementAgeTarget(retirementAge)
                .eligibleAt(eligibleAt).totalContributions("0.00")
                .totalEmployerContributions("0.00").totalCitizenContributions("0.00")
                .status(AccountStatus.ACCUMULATING).build());

        notificationClient.send(req.getCitizenNationalId(), "PENSION_ACCOUNT_OPENED", "EN",
                Map.of("accountCode", code, "eligibleAt", eligibleAt.toString()));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public PensionAccountResponse findByCode(String accountCode) {
        return toResponse(findOrThrow(accountCode));
    }

    @Transactional(readOnly = true)
    public PensionAccountResponse findByNin(String nationalId) {
        return toResponse(repo.findByCitizenNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("No pension account for: " + nationalId)));
    }

    @Transactional(readOnly = true)
    public EligibilityResponse getEligibility(String nationalId) {
        PensionAccount account = repo.findByCitizenNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("No pension account for: " + nationalId));
        long monthsContributed = contributionRepo.countByAccountCodeAndStatus(
                account.getAccountCode(), ContributionStatus.VERIFIED);
        BigDecimal total = parseBigDecimal(account.getTotalContributions());
        return eligibilityCalc.calculate(account, monthsContributed, total);
    }

    @Transactional
    public PensionAccountResponse updateStatus(String accountCode, AccountStatus status) {
        PensionAccount a = findOrThrow(accountCode);
        a.setStatus(status);
        return toResponse(repo.save(a));
    }

    @Transactional
    public void handleDeceased(String deceasedNationalId) {
        repo.findByCitizenNationalId(deceasedNationalId).ifPresent(account -> {
            account.setStatus(AccountStatus.CLOSED);
            repo.save(account);
            List<PensionPayment> scheduled = paymentRepo.findByStatusAndPaymentMonth(
                    PaymentStatus.SCHEDULED, currentMonth());
            scheduled.stream()
                    .filter(p -> p.getAccountCode().equals(account.getAccountCode()))
                    .forEach(p -> { p.setStatus(PaymentStatus.FAILED); p.setNotes("Account closed — citizen deceased"); paymentRepo.save(p); });
            notificationClient.send(deceasedNationalId, "PENSION_ACCOUNT_CLOSED", "EN",
                    Map.of("accountCode", account.getAccountCode()));
            log.info("[PENSION] Account {} closed due to death of NIN={}", account.getAccountCode(), deceasedNationalId);
        });
    }

    public PensionAccount findOrThrowEntity(String accountCode) { return findOrThrow(accountCode); }

    private PensionAccount findOrThrow(String accountCode) {
        return repo.findByAccountCode(accountCode)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountCode));
    }

    private String currentMonth() {
        return LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue());
    }

    static BigDecimal parseBigDecimal(String val) {
        try { return val == null ? BigDecimal.ZERO : new BigDecimal(val); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }

    private PensionAccountResponse toResponse(PensionAccount a) {
        return new PensionAccountResponse(a.getAccountId(), a.getAccountCode(), a.getCitizenNationalId(),
                a.getOpenedAt(), a.getEmploymentStartDate(), a.getBirthDate(), a.getGender(),
                a.getRetirementAgeTarget(), a.getEligibleAt(), a.getTotalContributions(),
                a.getTotalEmployerContributions(), a.getTotalCitizenContributions(), a.getStatus().name());
    }
}
