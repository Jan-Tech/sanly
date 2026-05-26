package com.sanly.appointments.scheduler;

import com.sanly.appointments.client.NotificationClient;
import com.sanly.appointments.entity.Appointment;
import com.sanly.appointments.entity.AppointmentStatus;
import com.sanly.appointments.repository.AppointmentRepository;
import com.sanly.appointments.repository.GovernmentOfficeRepository;
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
public class ReminderScheduler {

    private final AppointmentRepository appointmentRepo;
    private final GovernmentOfficeRepository officeRepo;
    private final NotificationClient notificationClient;

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void sendReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> appointments = appointmentRepo
                .findByStatusAndAppointmentDateAndReminderSentFalse(AppointmentStatus.BOOKED, tomorrow);

        log.info("Sending reminders for {} appointments tomorrow", appointments.size());

        for (Appointment apt : appointments) {
            String officeName = officeRepo.findByOfficeCode(apt.getOfficeCode())
                    .map(o -> o.getName())
                    .orElse(apt.getOfficeCode());

            notificationClient.send(
                    apt.getCitizenNationalId(),
                    "APPOINTMENT_REMINDER",
                    "EN",
                    Map.of(
                            "appointmentCode", apt.getAppointmentCode(),
                            "officeName", officeName,
                            "time", apt.getSlotTime().toString()
                    )
            );
            apt.setReminderSent(true);
            appointmentRepo.save(apt);
        }
    }
}
