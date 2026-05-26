package com.sanly.analytics.scheduler;

import com.sanly.analytics.client.NotificationClient;
import com.sanly.analytics.dto.request.GenerateReportRequest;
import com.sanly.analytics.entity.ReportType;
import com.sanly.analytics.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyReportScheduler {

    private final ReportGenerationService reportService;
    private final NotificationClient notificationClient;

    /** Every Monday at 07:00 — generates FULL_GOVERNMENT_REPORT for the past 7 days. */
    @Scheduled(cron = "0 0 7 * * MON")
    public void generateWeeklyReport() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now().minusDays(1);
        log.info("Generating weekly government report: {} to {}", from, to);
        GenerateReportRequest req = new GenerateReportRequest(ReportType.FULL_GOVERNMENT_REPORT, from, to);
        reportService.initiateReport(req, null);
        notificationClient.send(null, "WEEKLY_REPORT_READY", "EN",
                Map.of("weekStart", from.toString(), "weekEnd", to.toString()));
    }
}
