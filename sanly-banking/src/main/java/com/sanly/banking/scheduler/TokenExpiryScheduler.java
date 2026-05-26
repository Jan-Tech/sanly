package com.sanly.banking.scheduler;

import com.sanly.banking.entity.ConsentToken;
import com.sanly.banking.repository.ConsentTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenExpiryScheduler {

    private final ConsentTokenRepository tokenRepo;

    /**
     * Runs every 15 minutes. Marks ACTIVE tokens past their expiry as EXPIRED.
     */
    @Scheduled(fixedRate = 900_000)
    @Transactional
    public void expireOldTokens() {
        List<ConsentToken> expired = tokenRepo.findByStatusAndExpiresAtBefore("ACTIVE", LocalDateTime.now());

        if (expired.isEmpty()) return;

        log.info("Expiring {} consent tokens", expired.size());

        for (ConsentToken t : expired) {
            t.setStatus("EXPIRED");
            tokenRepo.save(t);
        }
    }
}
