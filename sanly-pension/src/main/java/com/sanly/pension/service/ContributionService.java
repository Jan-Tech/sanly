package com.sanly.pension.service;

import com.sanly.pension.client.NotificationClient;
import com.sanly.pension.config.UserDetailsImpl;
import com.sanly.pension.dto.request.SubmitContributionRequest;
import com.sanly.pension.dto.request.VerifyContributionRequest;
import com.sanly.pension.dto.response.ContributionResponse;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContributionService {
    private final ContributionRecordRepository repo;
    private final PensionAccountRepository accountRepo;
    private final EmployerRepository employerRepo;
    private final NotificationClient notificationClient;

    @Transactional
    public ContributionResponse submit(SubmitContributionRequest req) {
        PensionAccount account = accountRepo.findByAccountCode(req.getAccountCode())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + req.getAccountCode()));
        if (account.getStatus() == AccountStatus.CLOSED || account.getStatus() == AccountStatus.SUSPENDED)
            throw new BusinessException("Account is not active: " + account.getStatus());

        UserDetailsImpl ud = authenticatedUser();
        String employerCode = req.getEmployerCode();

        if ("EMPLOYER".equals(ud.getRole())) {
            if (!ud.getEmployerCode().equals(employerCode))
                throw new BusinessException("Employer can only submit contributions for their own employees");
        }

        ContributionRecord record = repo.save(ContributionRecord.builder()
                .accountCode(req.getAccountCode()).employerCode(employerCode)
                .contributionMonth(req.getContributionMonth())
                .employerAmount(req.getEmployerAmount()).citizenAmount(req.getCitizenAmount())
                .totalAmount(req.getTotalAmount()).submittedAt(LocalDateTime.now())
                .submittedByOfficerId(ud.getOfficerId()).status(ContributionStatus.SUBMITTED).build());

        String employerName = employerCode != null
                ? employerRepo.findByEmployerCode(employerCode).map(Employer::getBusinessName).orElse(employerCode)
                : "Self";
        notificationClient.send(account.getCitizenNationalId(), "CONTRIBUTION_RECEIVED", "EN", Map.of(
                "amount", req.getTotalAmount(), "contributionMonth", req.getContributionMonth(),
                "employerName", employerName));
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<ContributionResponse> findByAccount(String accountCode) {
        return repo.findByAccountCodeOrderByContributionMonthDesc(accountCode)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ContributionResponse> findByEmployer(String employerCode) {
        return repo.findByEmployerCodeOrderByContributionMonthDesc(employerCode)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<Employer> findEmployersWithMissingContributions(String month) {
        List<String> submitted = repo.findEmployerCodesWithContributionForMonth(month);
        return employerRepo.findByStatus(EmployerStatus.ACTIVE).stream()
                .filter(e -> !submitted.contains(e.getEmployerCode())).toList();
    }

    @Transactional
    public ContributionResponse verify(Long contributionId, VerifyContributionRequest req) {
        ContributionRecord record = repo.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found: " + contributionId));
        if (record.getStatus() != ContributionStatus.SUBMITTED)
            throw new BusinessException("Contribution is not in SUBMITTED status");

        record.setStatus(req.getStatus());
        if (req.getStatus() == ContributionStatus.REJECTED)
            record.setRejectionReason(req.getRejectionReason());

        if (req.getStatus() == ContributionStatus.VERIFIED) {
            accountRepo.findByAccountCode(record.getAccountCode()).ifPresent(account -> {
                BigDecimal total = PensionAccountService.parseBigDecimal(account.getTotalContributions());
                BigDecimal empTotal = PensionAccountService.parseBigDecimal(account.getTotalEmployerContributions());
                BigDecimal citTotal = PensionAccountService.parseBigDecimal(account.getTotalCitizenContributions());
                BigDecimal amt = PensionAccountService.parseBigDecimal(record.getTotalAmount());
                BigDecimal empAmt = PensionAccountService.parseBigDecimal(record.getEmployerAmount());
                BigDecimal citAmt = PensionAccountService.parseBigDecimal(record.getCitizenAmount());
                account.setTotalContributions(total.add(amt).toPlainString());
                account.setTotalEmployerContributions(empTotal.add(empAmt != null ? empAmt : BigDecimal.ZERO).toPlainString());
                account.setTotalCitizenContributions(citTotal.add(citAmt != null ? citAmt : BigDecimal.ZERO).toPlainString());
                accountRepo.save(account);
            });
        }
        return toResponse(repo.save(record));
    }

    private UserDetailsImpl authenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetailsImpl) auth.getPrincipal();
    }

    private ContributionResponse toResponse(ContributionRecord r) {
        return new ContributionResponse(r.getContributionId(), r.getAccountCode(), r.getEmployerCode(),
                r.getContributionMonth(), r.getEmployerAmount(), r.getCitizenAmount(), r.getTotalAmount(),
                r.getSubmittedAt(), r.getSubmittedByOfficerId(), r.getStatus().name(), r.getRejectionReason());
    }
}
