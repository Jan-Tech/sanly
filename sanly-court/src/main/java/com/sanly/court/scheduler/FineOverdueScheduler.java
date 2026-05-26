package com.sanly.court.scheduler;

import com.sanly.court.client.NotificationClient;
import com.sanly.court.entity.CourtFine;
import com.sanly.court.entity.FineStatus;
import com.sanly.court.repository.CourtFineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FineOverdueScheduler {

    private final CourtFineRepository fineRepo;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void markOverdueFines() {
        List<CourtFine> overdue = fineRepo.findOutstandingOverdue(LocalDate.now());
        log.info("Fine overdue scheduler: {} fines to mark as OVERDUE", overdue.size());

        for (CourtFine fine : overdue) {
            try {
                fine.setStatus(FineStatus.OVERDUE);
                fineRepo.save(fine);
                notificationClient.send(fine.getCitizenNationalId(), "FINE_OVERDUE", "TK",
                        Map.of("fineCode", fine.getFineCode(),
                               "amount", fine.getAmount(),
                               "dueDate", fine.getDueDate().toString()));
            } catch (Exception e) {
                log.warn("Failed to mark fine {} as overdue: {}", fine.getFineCode(), e.getMessage());
            }
        }
    }
}
