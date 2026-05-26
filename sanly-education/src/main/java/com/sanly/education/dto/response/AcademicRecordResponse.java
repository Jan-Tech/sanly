package com.sanly.education.dto.response;

import com.sanly.education.entity.AcademicRecord;

import java.time.LocalDateTime;
import java.util.UUID;

public record AcademicRecordResponse(
        UUID recordId,
        String citizenNationalId,
        String institutionCode,
        String academicYear,
        String grade,
        String gpa,
        String notes,
        UUID recordedByOfficerId,
        LocalDateTime createdAt
) {
    public static AcademicRecordResponse from(AcademicRecord r) {
        return new AcademicRecordResponse(r.getRecordId(), r.getCitizenNationalId(),
                r.getInstitutionCode(), r.getAcademicYear(), r.getGrade(), r.getGpa(),
                r.getNotes(), r.getRecordedByOfficerId(), r.getCreatedAt());
    }
}
