package com.sanly.pension.scheduler;

import com.sanly.pension.client.BridgePublisherService;
import com.sanly.pension.client.NotificationClient;
import com.sanly.pension.entity.AccountStatus;
import com.sanly.pension.entity.ContributionStatus;
import com.sanly.pension.entity.PensionAccount;
import com.sanly.pension.repository.ContributionRecordRepository;
import com.sanly.pension.repository.PensionAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EligibilityCheckScheduler {
    private final PensionAccountRepository accountRepo;
    private final ContributionRecordRepository contributionRepo;
    private final BridgePublisherService bridgePublisher;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 0 6 * * *")
    public void checkEligibility() {
        List<PensionAccount> nowEligible = accountRepo.findNowEligible(LocalDate.now());
        log.info("[ELIGIBILITY-SCHEDULER] {} accounts newly eligible", nowEligible.size());
        for (PensionAccount account : nowEligible) {
            long months = contributionRepo.countByAccountCodeAndStatus(
                    account.getAccountCode(), ContributionStatus.VERIFIED);
            if (months < 180) continue; // minimum 15 years

            account.setStatus(AccountStatus.ELIGIBLE);
            accountRepo.save(account);
            bridgePublisher.publishPensionStatus(account, null, (int) months);
            notificationClient.send(account.getCitizenNationalId(), "PENSION_NOW_ELIGIBLE", "EN",
                    Map.of("accountCode", account.getAccountCode()));
        }
    }
}
