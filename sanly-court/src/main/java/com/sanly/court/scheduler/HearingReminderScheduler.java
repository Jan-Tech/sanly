package com.sanly.court.scheduler;

import com.sanly.court.client.NotificationClient;
import com.sanly.court.entity.CourtCase;
import com.sanly.court.repository.CourtCaseRepository;
import com.sanly.court.repository.CourtRepository;
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
public class HearingReminderScheduler {

    private final CourtCaseRepository caseRepo;
    private final CourtRepository courtRepo;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 0 7 * * *")
    public void sendHearingReminders() {
        LocalDate cutoff = LocalDate.now().plusDays(3);
        List<CourtCase> upcoming = caseRepo.findUpcomingHearings(cutoff);
        log.info("Hearing reminder scheduler: {} cases with upcoming hearings", upcoming.size());

        for (CourtCase courtCase : upcoming) {
            try {
                String courtName = courtRepo.findByCourtCode(courtCase.getCourtCode())
                        .map(c -> c.getName()).orElse(courtCase.getCourtCode());

                Map<String, String> meta = Map.of(
                        "caseNumber", courtCase.getCaseNumber(),
                        "courtName", courtName,
                        "hearingDate", courtCase.getHearingDate().toString()
                );

                notificationClient.send(courtCase.getPlaintiffNationalId(), "HEARING_REMINDER", "TK", meta);
                notificationClient.send(courtCase.getDefendantNationalId(), "HEARING_REMINDER", "TK", meta);
            } catch (Exception e) {
                log.warn("Failed to send hearing reminder for case {}: {}",
                        courtCase.getCaseNumber(), e.getMessage());
            }
        }
    }
}
