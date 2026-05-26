package com.sanly.appointments.scheduler;

import com.sanly.appointments.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoShowMarkingScheduler {

    private final AppointmentRepository appointmentRepo;

    @Scheduled(fixedRate = 1_800_000)
    @Transactional
    public void markNoShows() {
        LocalDate today = LocalDate.now();
        // Mark as no-show if slot time + 30 minutes has passed
        LocalTime cutoff = LocalTime.now().minusMinutes(30);
        int count = appointmentRepo.markNoShows(today, cutoff);
        if (count > 0) {
            log.info("Marked {} appointments as NO_SHOW for today before {}", count, cutoff);
        }
    }
}
