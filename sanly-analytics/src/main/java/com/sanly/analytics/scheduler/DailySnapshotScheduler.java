package com.sanly.analytics.scheduler;

import com.sanly.analytics.client.NotificationClient;
import com.sanly.analytics.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailySnapshotScheduler {

    private final SnapshotService snapshotService;
    private final NotificationClient notificationClient;

    /** Daily at 23:30 — finalizes today's snapshot and notifies admins. */
    @Scheduled(cron = "0 30 23 * * *")
    public void finalizeDailySnapshot() {
        log.info("Daily snapshot finalization starting for {}", LocalDate.now());
        snapshotService.getOrCreateToday();
        notificationClient.send(null, "DAILY_REPORT_READY", "EN",
                Map.of("date", LocalDate.now().toString()));
        log.info("Daily snapshot finalized for {}", LocalDate.now());
    }
}
