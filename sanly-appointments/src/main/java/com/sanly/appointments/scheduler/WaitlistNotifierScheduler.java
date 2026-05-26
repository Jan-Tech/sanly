package com.sanly.appointments.scheduler;

import com.sanly.appointments.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitlistNotifierScheduler {

    private final WaitlistService waitlistService;

    /** Every hour — checks NOTIFIED entries older than 2 hours and re-notifies next citizen. */
    @Scheduled(fixedRate = 3_600_000)
    public void processExpiredNotifications() {
        log.debug("Running waitlist notification expiry check");
        waitlistService.processExpiredNotifications();
    }
}
