package com.sanly.education.dto.response;

import com.sanly.education.entity.EducationOfficer;
import com.sanly.education.entity.OfficerRole;
import com.sanly.education.entity.OfficerStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OfficerResponse(
        UUID officerId,
        String nationalId,
        String firstName,
        String lastName,
        String username,
        String institutionCode,
        OfficerRole role,
        OfficerStatus status,
        LocalDateTime createdAt
) {
    public static OfficerResponse from(EducationOfficer o) {
        return new OfficerResponse(o.getOfficerId(), o.getNationalId(), o.getFirstName(),
                o.getLastName(), o.getUsername(), o.getInstitutionCode(),
                o.getRole(), o.getStatus(), o.getCreatedAt());
    }
}
