package com.sanly.social.scheduler;

import com.sanly.social.client.NotificationClient;
import com.sanly.social.entity.PensionAccount;
import com.sanly.social.entity.PensionStatus;
import com.sanly.social.repository.PensionAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PensionEligibilityChecker {

    private final PensionAccountRepository pensionRepo;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void checkEligibility() {
        List<PensionAccount> newlyEligible = pensionRepo.findNewlyEligible(LocalDate.now());
        log.info("Pension eligibility check: {} accounts newly eligible", newlyEligible.size());

        for (PensionAccount account : newlyEligible) {
            try {
                account.setStatus(PensionStatus.ELIGIBLE);
                pensionRepo.save(account);
                notificationClient.send(account.getCitizenNationalId(), "PENSION_ELIGIBLE", "TK",
                        Map.of("eligibleAt", account.getEligibleAt().toString(),
                               "contributions", account.getTotalContributions() != null ? account.getTotalContributions() : "0"));
            } catch (Exception e) {
                log.warn("Failed to process pension eligibility for {}: {}",
                        account.getCitizenNationalId(), e.getMessage());
            }
        }
    }
}
