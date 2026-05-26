package com.sanly.customs.scheduler;

import com.sanly.customs.client.NotificationClient;
import com.sanly.customs.entity.CustomsDeclaration;
import com.sanly.customs.repository.CustomsDeclarationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DutiesReminderScheduler {

    private final CustomsDeclarationRepository declarationRepo;
    private final NotificationClient notificationClient;

    // Runs daily at 09:00 — finds SUBMITTED declarations > 3 days old with unpaid duties
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDutiesReminders() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
        List<CustomsDeclaration> overdue = declarationRepo.findSubmittedWithUnpaidDutiesOlderThan(cutoff);
        log.info("Duties reminder job: {} declarations with overdue duties", overdue.size());

        for (CustomsDeclaration decl : overdue) {
            try {
                String recipientId = decl.getDeclarantNationalId() != null
                        ? decl.getDeclarantNationalId()
                        : decl.getDeclarantBusinessNumber();
                if (recipientId == null) continue;

                notificationClient.send(recipientId, "CUSTOMS_DUTIES_REMINDER", "TK",
                        Map.of(
                                "declarationCode", decl.getDeclarationCode(),
                                "amount", decl.getDutiesOwed() != null ? decl.getDutiesOwed() : "TBD",
                                "portCode", decl.getPortCode()
                        ));
            } catch (Exception e) {
                log.warn("Failed to send duties reminder for {}: {}",
                        decl.getDeclarationCode(), e.getMessage());
            }
        }
    }
}
