package com.sanly.medical.service;

import com.sanly.medical.client.NotificationClient;
import com.sanly.medical.entity.VaccinationAppointment;
import com.sanly.medical.repository.VaccinationScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Daily job: finds vaccination appointments due within 7 days and notifies parents.
 * Parent NINs are stored on VaccinationSchedule when the schedule is created at birth.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VaccinationReminderService {

    private final VaccinationService           vaccinationService;
    private final VaccinationScheduleRepository scheduleRepository;
    private final NotificationClient            notificationClient;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void sendVaccinationReminders() {
        List<VaccinationAppointment> dueSoon = vaccinationService.findDueSoon(7);
        log.info("Vaccination reminder job: {} appointments due within 7 days", dueSoon.size());

        for (VaccinationAppointment appt : dueSoon) {
            scheduleRepository.findByCitizenNationalId(appt.getCitizenNationalId())
                    .ifPresent(schedule -> {
                        Map<String, String> meta = Map.of(
                                "childNationalId", appt.getCitizenNationalId(),
                                "childFullName",   "Child " + appt.getCitizenNationalId(),
                                "vaccineName",     appt.getVaccineName(),
                                "dueDate",         appt.getDueDate().toString()
                        );
                        if (schedule.getMotherNationalId() != null) {
                            notificationClient.send(schedule.getMotherNationalId(),
                                    "VACCINATION_DUE_SOON", "EN", meta);
                        }
                        if (schedule.getFatherNationalId() != null) {
                            notificationClient.send(schedule.getFatherNationalId(),
                                    "VACCINATION_DUE_SOON", "EN", meta);
                        }
                    });
        }
    }
}
