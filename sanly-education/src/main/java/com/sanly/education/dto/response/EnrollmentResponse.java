package com.sanly.education.dto.response;

import com.sanly.education.entity.Enrollment;
import com.sanly.education.entity.EnrollmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EnrollmentResponse(
        UUID enrollmentId,
        String citizenNationalId,
        String institutionCode,
        LocalDate enrollmentDate,
        Integer expectedGraduationYear,
        String programName,
        EnrollmentStatus status,
        LocalDateTime createdAt
) {
    public static EnrollmentResponse from(Enrollment e) {
        return new EnrollmentResponse(e.getEnrollmentId(), e.getCitizenNationalId(),
                e.getInstitutionCode(), e.getEnrollmentDate(), e.getExpectedGraduationYear(),
                e.getProgramName(), e.getStatus(), e.getCreatedAt());
    }
}
