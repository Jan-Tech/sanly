package com.sanly.analytics.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HourlyServiceStatScheduler {

    /** Every hour — services push stats via AnalyticsClient; this scheduler logs a heartbeat. */
    @Scheduled(fixedRate = 3_600_000)
    public void hourlyCheck() {
        log.debug("Hourly analytics stat check — awaiting service push data");
    }
}
