package com.sanly.registry.scheduler;

import com.sanly.registry.repository.ActiveSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionExpiryScheduler {
    private final ActiveSessionRepository sessionRepository;

    @Scheduled(fixedDelay = 900_000) // every 15 minutes
    @Transactional
    public void expireSessions() {
        int expired = sessionRepository.expireOldSessions(LocalDateTime.now());
        if (expired > 0) log.info("[SESSION-EXPIRY] Revoked {} expired sessions", expired);
    }
}
