package com.sanly.medical.service;

import com.sanly.medical.entity.VaccinationAppointment;
import com.sanly.medical.entity.VaccinationSchedule;
import com.sanly.medical.entity.VaccinationStatus;
import com.sanly.medical.repository.VaccinationAppointmentRepository;
import com.sanly.medical.repository.VaccinationScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaccinationService {

    private final VaccinationScheduleRepository    scheduleRepository;
    private final VaccinationAppointmentRepository appointmentRepository;

    /**
     * Turkmenistan childhood vaccination milestones.
     * Pairs: (vaccine name, days after birth).
     */
    private static final List<Object[]> MILESTONES = List.of(
            new Object[]{"BCG (Tuberculosis) + HepB-1",             0},
            new Object[]{"HepB-2 + DTaP-1 + Hib-1 + IPV-1",        60},
            new Object[]{"DTaP-2 + Hib-2 + IPV-2",                  120},
            new Object[]{"DTaP-3 + Hib-3 + HepB-3 + IPV-3",        180},
            new Object[]{"MMR-1 (Measles, Mumps, Rubella)",         365},
            new Object[]{"DTaP-4 + Hib-4 + IPV-4",                 548},
            new Object[]{"MMR-2 + DTaP-5 + IPV-5",                 1825}
    );

    @Transactional
    public VaccinationSchedule createSchedule(String citizenNationalId, LocalDate birthDate,
                                               String motherNationalId, String fatherNationalId) {
        if (scheduleRepository.existsByCitizenNationalId(citizenNationalId)) {
            log.info("Vaccination schedule already exists for NIN={}", citizenNationalId);
            return scheduleRepository.findByCitizenNationalId(citizenNationalId).orElseThrow();
        }

        VaccinationSchedule schedule = VaccinationSchedule.builder()
                .citizenNationalId(citizenNationalId)
                .motherNationalId(motherNationalId)
                .fatherNationalId(fatherNationalId)
                .scheduleStatus("ACTIVE")
                .build();
        VaccinationSchedule saved = scheduleRepository.save(schedule);

        for (Object[] milestone : MILESTONES) {
            String vaccineName = (String) milestone[0];
            int daysOffset     = (Integer) milestone[1];
            VaccinationAppointment appt = VaccinationAppointment.builder()
                    .scheduleId(saved.getScheduleId())
                    .citizenNationalId(citizenNationalId)
                    .vaccineName(vaccineName)
                    .dueDate(birthDate.plusDays(daysOffset))
                    .status(VaccinationStatus.PENDING)
                    .build();
            appointmentRepository.save(appt);
        }

        log.info("Vaccination schedule created for NIN={} with {} milestones",
                citizenNationalId, MILESTONES.size());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<VaccinationAppointment> getScheduleForCitizen(String nationalId) {
        return appointmentRepository.findByCitizenNationalIdOrderByDueDateAsc(nationalId);
    }

    @Transactional
    public VaccinationAppointment markCompleted(UUID appointmentId, Long doctorId) {
        VaccinationAppointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));
        appt.setStatus(VaccinationStatus.COMPLETED);
        appt.setCompletedAt(LocalDateTime.now());
        appt.setCompletedByDoctorId(doctorId);
        VaccinationAppointment saved = appointmentRepository.save(appt);
        log.info("Vaccination {} marked complete by doctor={}", appt.getVaccineName(), doctorId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<VaccinationAppointment> findDueSoon(int withinDays) {
        LocalDate today = LocalDate.now();
        return appointmentRepository.findDueSoon(today, today.plusDays(withinDays));
    }
}
