package com.sanly.social.service;

import com.sanly.social.client.CitizenRegistryClient;
import com.sanly.social.client.NotificationClient;
import com.sanly.social.dto.request.AutoTriggerRequest;
import com.sanly.social.dto.request.ContributeRequest;
import com.sanly.social.dto.request.OpenPensionRequest;
import com.sanly.social.dto.response.PensionEligibilityResponse;
import com.sanly.social.dto.response.PensionResponse;
import com.sanly.social.entity.BenefitType;
import com.sanly.social.entity.PensionAccount;
import com.sanly.social.entity.PensionStatus;
import com.sanly.social.exception.DuplicateResourceException;
import com.sanly.social.exception.InvalidOperationException;
import com.sanly.social.exception.RecordNotFoundException;
import com.sanly.social.repository.PensionAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PensionService {

    private final PensionAccountRepository pensionRepo;
    private final CitizenRegistryClient citizenClient;
    private final BenefitClaimService claimService;
    private final NotificationClient notificationClient;

    @Transactional
    public PensionResponse open(OpenPensionRequest req) {
        citizenClient.verify(req.citizenNationalId());
        if (pensionRepo.existsByCitizenNationalId(req.citizenNationalId()))
            throw new DuplicateResourceException("Pension account already exists: " + req.citizenNationalId());

        LocalDate eligibleAt = calculateEligibleAt(req.citizenNationalId(), req.contributionStartDate());

        PensionAccount account = PensionAccount.builder()
                .citizenNationalId(req.citizenNationalId())
                .contributionStartDate(req.contributionStartDate())
                .totalContributions("0")
                .eligibleAt(eligibleAt)
                .status(PensionStatus.ACCUMULATING)
                .build();
        return PensionResponse.from(pensionRepo.save(account));
    }

    public PensionResponse getDetails(String nationalId) {
        return pensionRepo.findByCitizenNationalId(nationalId)
                .map(PensionResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("No pension account for: " + nationalId));
    }

    @Transactional
    public PensionResponse contribute(String nationalId, ContributeRequest req) {
        PensionAccount account = pensionRepo.findByCitizenNationalId(nationalId)
                .orElseThrow(() -> new RecordNotFoundException("No pension account for: " + nationalId));
        if (account.getStatus() == PensionStatus.CLOSED)
            throw new InvalidOperationException("Pension account is closed: " + nationalId);

        BigDecimal current = parseSafe(account.getTotalContributions());
        BigDecimal added = parseSafe(req.amount());
        account.setTotalContributions(current.add(added).toPlainString());
        return PensionResponse.from(pensionRepo.save(account));
    }

    public PensionEligibilityResponse checkEligibility(String nationalId) {
        PensionAccount account = pensionRepo.findByCitizenNationalId(nationalId)
                .orElseThrow(() -> new RecordNotFoundException("No pension account for: " + nationalId));

        boolean eligible = !LocalDate.now().isBefore(account.getEligibleAt());
        long yearsRemaining = eligible ? 0 : ChronoUnit.YEARS.between(LocalDate.now(), account.getEligibleAt());
        return new PensionEligibilityResponse(eligible, account.getEligibleAt(), yearsRemaining, account.getStatus());
    }

    @Transactional
    public PensionResponse startPaying(String nationalId) {
        PensionAccount account = pensionRepo.findByCitizenNationalId(nationalId)
                .orElseThrow(() -> new RecordNotFoundException("No pension account for: " + nationalId));

        if (account.getStatus() != PensionStatus.ELIGIBLE)
            throw new InvalidOperationException("Pension account is not ELIGIBLE: " + nationalId);

        account.setStatus(PensionStatus.PAYING);
        pensionRepo.save(account);

        claimService.autoTrigger(new AutoTriggerRequest(
                nationalId, BenefitType.PENSION, null, "Pension payment started"));

        return PensionResponse.from(account);
    }

    private LocalDate calculateEligibleAt(String nationalId, LocalDate startDate) {
        // NIN gender digit: position 6 (0-indexed) — odd = male, even = female
        // Males retire at 62 (25 years contribution), females at 57 (20 years)
        int retirementYears;
        try {
            char genderDigit = nationalId.charAt(6);
            retirementYears = (Character.getNumericValue(genderDigit) % 2 != 0) ? 25 : 20;
        } catch (Exception e) {
            retirementYears = 25;
        }
        return startDate.plusYears(retirementYears);
    }

    private BigDecimal parseSafe(String value) {
        try {
            return new BigDecimal(value == null ? "0" : value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
