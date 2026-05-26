package com.sanly.banking.scheduler;

import com.sanly.banking.client.NotificationClient;
import com.sanly.banking.entity.ConsentRequest;
import com.sanly.banking.repository.ConsentRequestRepository;
import com.sanly.banking.repository.RegisteredBankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsentExpiryScheduler {

    private final ConsentRequestRepository consentRepo;
    private final RegisteredBankRepository bankRepo;
    private final NotificationClient       notificationClient;

    /**
     * Runs every 5 minutes. Marks PENDING consents older than 10 minutes as EXPIRED.
     */
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void expireOldConsentRequests() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<ConsentRequest> expired = consentRepo.findByStatusAndRequestedAtBefore("PENDING", cutoff);

        if (expired.isEmpty()) return;

        log.info("Expiring {} stale PENDING consent requests", expired.size());

        for (ConsentRequest req : expired) {
            req.setStatus("EXPIRED");
            consentRepo.save(req);

            try {
                String bankName = bankRepo.findByBankCode(req.getBankCode())
                        .map(b -> b.getBankName())
                        .orElse(req.getBankCode());

                notificationClient.send(req.getCitizenNationalId(), "BANKING_CONSENT_EXPIRED", "EN",
                        Map.of("bankName", bankName, "consentCode", req.getConsentCode()));
            } catch (Exception e) {
                log.warn("Failed to send expiry notification for consent {}: {}", req.getConsentCode(), e.getMessage());
            }
        }

        log.info("Expired {} consent requests", expired.size());
    }
}
