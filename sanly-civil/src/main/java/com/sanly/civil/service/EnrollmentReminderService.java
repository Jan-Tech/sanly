package com.sanly.civil.service;

import com.sanly.civil.client.NotificationClient;
import com.sanly.civil.entity.EnrollmentStatus;
import com.sanly.civil.entity.PendingEnrollment;
import com.sanly.civil.repository.PendingEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Runs every September 1st at 9 AM.
 * Finds all children turning 6 this year (expectedSchoolYear == current year)
 * and notifies parents to enroll them in school.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentReminderService {

    private final PendingEnrollmentRepository enrollmentRepository;
    private final NotificationClient          notificationClient;

    @Scheduled(cron = "0 0 9 1 9 *")
    @Transactional
    public void sendEnrollmentReminders() {
        int currentYear = LocalDateTime.now().getYear();
        List<PendingEnrollment> due = enrollmentRepository
                .findByExpectedSchoolYearAndStatus(currentYear, EnrollmentStatus.QUEUED);

        log.info("Enrollment reminder job: {} children due for school enrollment in {}",
                due.size(), currentYear);

        for (PendingEnrollment enrollment : due) {
            Map<String, String> meta = Map.of(
                    "childFullName",    enrollment.getChildFullName() != null
                                            ? enrollment.getChildFullName() : "your child",
                    "childNationalId",  enrollment.getChildNationalId(),
                    "schoolYear",       String.valueOf(currentYear)
            );

            boolean notified = false;
            if (enrollment.getMotherNationalId() != null) {
                try {
                    notificationClient.send(enrollment.getMotherNationalId(),
                            "SCHOOL_ENROLLMENT_REMINDER", "EN", meta);
                    notified = true;
                } catch (Exception ex) {
                    log.warn("Enrollment notification to mother failed: {}", ex.getMessage());
                }
            }
            if (enrollment.getFatherNationalId() != null) {
                try {
                    notificationClient.send(enrollment.getFatherNationalId(),
                            "SCHOOL_ENROLLMENT_REMINDER", "EN", meta);
                    notified = true;
                } catch (Exception ex) {
                    log.warn("Enrollment notification to father failed: {}", ex.getMessage());
                }
            }

            if (notified) {
                enrollment.setStatus(EnrollmentStatus.NOTIFIED);
                enrollment.setNotifiedAt(LocalDateTime.now());
                enrollmentRepository.save(enrollment);
            }
        }
    }
}
