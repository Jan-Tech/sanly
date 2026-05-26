package com.sanly.appointments.scheduler;

import com.sanly.appointments.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitlistExpiryScheduler {

    private final WaitlistService waitlistService;

    /** Daily at midnight — expires WAITING entries older than 30 days. */
    @Scheduled(cron = "0 0 0 * * *")
    public void expireOldWaitlistEntries() {
        int expired = waitlistService.expireOldEntries();
        log.info("Waitlist expiry run: {} entries expired", expired);
    }
}
