package com.sanly.business.dto;

import com.sanly.business.entity.OfficerStatus;
import com.sanly.business.entity.RegistrationOfficer;

import java.time.Instant;
import java.util.Set;

public record OfficerResponse(Long id, String username, String fullName, String nationalId,
                               String officeRegion, OfficerStatus status, Set<String> roles, Instant createdAt) {
    public static OfficerResponse from(RegistrationOfficer o) {
        return new OfficerResponse(o.getId(), o.getUsername(), o.getFullName(), o.getNationalId(),
                o.getOfficeRegion(), o.getStatus(), o.getRoles(), o.getCreatedAt());
    }
}
