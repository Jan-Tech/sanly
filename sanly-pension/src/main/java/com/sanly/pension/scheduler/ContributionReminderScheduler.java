package com.sanly.pension.scheduler;

import com.sanly.pension.client.NotificationClient;
import com.sanly.pension.entity.Employer;
import com.sanly.pension.service.ContributionService;
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
public class ContributionReminderScheduler {
    private final ContributionService contributionService;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 0 9 25 * *")
    public void sendContributionReminders() {
        String month = currentMonth();
        List<Employer> missing = contributionService.findEmployersWithMissingContributions(month);
        log.info("[CONTRIBUTION-SCHEDULER] {} employers missing contributions for month={}", missing.size(), month);
        for (Employer employer : missing) {
            notificationClient.send(employer.getContactNationalId(), "CONTRIBUTION_DUE_REMINDER", "EN",
                    Map.of("month", month, "employerName", employer.getBusinessName()));
        }
    }

    private String currentMonth() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }
}
