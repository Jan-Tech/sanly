package com.sanly.social.scheduler;

import com.sanly.social.client.NotificationClient;
import com.sanly.social.entity.ClaimStatus;
import com.sanly.social.entity.UnemploymentRecord;
import com.sanly.social.entity.UnemploymentStatus;
import com.sanly.social.repository.BenefitClaimRepository;
import com.sanly.social.repository.UnemploymentRecordRepository;
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
public class UnemploymentExpiry {

    private final UnemploymentRecordRepository unemploymentRepo;
    private final BenefitClaimRepository claimRepo;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void expireStaleRegistrations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(12);
        List<UnemploymentRecord> expired = unemploymentRepo.findExpiredRegistrations(cutoff);
        log.info("Unemployment expiry job: {} registrations to expire", expired.size());

        for (UnemploymentRecord record : expired) {
            try {
                record.setStatus(UnemploymentStatus.EXPIRED);
                unemploymentRepo.save(record);

                claimRepo.findByCitizenNationalIdAndStatus(record.getCitizenNationalId(), ClaimStatus.ACTIVE)
                        .forEach(claim -> {
                            claim.setStatus(ClaimStatus.EXPIRED);
                            claimRepo.save(claim);
                        });

                notificationClient.send(record.getCitizenNationalId(), "UNEMPLOYMENT_BENEFIT_EXPIRED", "TK",
                        Map.of("reason", "12-month registration period expired",
                               "registeredAt", record.getRegisteredAt().toString()));
            } catch (Exception e) {
                log.warn("Failed to expire unemployment record for {}: {}",
                        record.getCitizenNationalId(), e.getMessage());
            }
        }
    }
}
