package com.sanly.education.service;

import com.sanly.education.client.CitizenRegistryClient;
import com.sanly.education.client.NotificationClient;
import com.sanly.education.config.UserDetailsImpl;
import com.sanly.education.dto.request.EnrollRequest;
import com.sanly.education.dto.response.EnrollmentResponse;
import com.sanly.education.entity.Enrollment;
import com.sanly.education.entity.EnrollmentStatus;
import com.sanly.education.entity.InstitutionStatus;
import com.sanly.education.exception.InvalidOperationException;
import com.sanly.education.exception.RecordNotFoundException;
import com.sanly.education.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final InstitutionService institutionService;
    private final CitizenRegistryClient citizenRegistryClient;
    private final NotificationClient notificationClient;

    @Transactional
    public EnrollmentResponse enroll(EnrollRequest req, Authentication auth) {
        var institution = institutionService.getOrThrow(req.institutionCode());
        if (institution.getStatus() != InstitutionStatus.ACTIVE) {
            throw new InvalidOperationException("Institution is not active: " + req.institutionCode());
        }

        // Officers can only enroll into their own institution
        if (auth != null) {
            UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
            if (user.getInstitutionCode() != null &&
                    !user.getInstitutionCode().equals(req.institutionCode())) {
                throw new InvalidOperationException("You can only enroll students in your own institution");
            }
        }

        citizenRegistryClient.verify(req.citizenNationalId());

        Enrollment enrollment = Enrollment.builder()
                .citizenNationalId(req.citizenNationalId())
                .institutionCode(req.institutionCode())
                .enrollmentDate(req.enrollmentDate())
                .expectedGraduationYear(req.expectedGraduationYear())
                .programName(req.programName())
                .status(EnrollmentStatus.ACTIVE)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);

        notificationClient.send(req.citizenNationalId(), "ENROLLMENT_CONFIRMED", "EN",
                Map.of(
                        "institutionName", institution.getName(),
                        "programName",     req.programName() != null ? req.programName() : "General",
                        "enrollmentDate",  req.enrollmentDate().toString()
                ));

        return EnrollmentResponse.from(saved);
    }

    @Transactional
    public EnrollmentResponse createPendingIntake(String childNationalId, int expectedSchoolYear) {
        // Skip if already has an enrollment from this creation path
        Enrollment enrollment = Enrollment.builder()
                .citizenNationalId(childNationalId)
                .institutionCode("PENDING")
                .enrollmentDate(LocalDate.of(expectedSchoolYear, 9, 1))
                .expectedGraduationYear(expectedSchoolYear + 4)
                .programName("Primary School")
                .status(EnrollmentStatus.PENDING)
                .build();
        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    public EnrollmentResponse findById(UUID id) {
        return EnrollmentResponse.from(enrollmentRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Enrollment not found: " + id)));
    }

    public List<EnrollmentResponse> findByCitizen(String nationalId) {
        return enrollmentRepository.findByCitizenNationalId(nationalId)
                .stream().map(EnrollmentResponse::from).toList();
    }

    public List<EnrollmentResponse> findPendingIntakes() {
        return enrollmentRepository.findByStatus(EnrollmentStatus.PENDING)
                .stream().map(EnrollmentResponse::from).toList();
    }

    @Transactional
    public EnrollmentResponse updateStatus(UUID id, EnrollmentStatus status, Authentication auth) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Enrollment not found: " + id));

        if (auth != null) {
            UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
            if (user.getInstitutionCode() != null &&
                    !user.getInstitutionCode().equals(enrollment.getInstitutionCode())) {
                throw new InvalidOperationException("You can only update enrollments in your own institution");
            }
        }

        enrollment.setStatus(status);
        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }
}
