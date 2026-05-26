package com.sanly.education.dto.response;

import com.sanly.education.entity.EducationInstitution;
import com.sanly.education.entity.InstitutionStatus;
import com.sanly.education.entity.InstitutionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InstitutionResponse(
        UUID institutionId,
        String institutionCode,
        String name,
        InstitutionType type,
        String region,
        String address,
        String licenseNumber,
        LocalDate accreditedUntil,
        InstitutionStatus status,
        LocalDateTime createdAt
) {
    public static InstitutionResponse from(EducationInstitution i) {
        return new InstitutionResponse(i.getInstitutionId(), i.getInstitutionCode(), i.getName(),
                i.getType(), i.getRegion(), i.getAddress(), i.getLicenseNumber(),
                i.getAccreditedUntil(), i.getStatus(), i.getCreatedAt());
    }
}
