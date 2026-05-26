package com.sanly.customs.dto.response;

import com.sanly.customs.entity.CustomsOfficer;
import com.sanly.customs.entity.OfficerRole;
import com.sanly.customs.entity.OfficerStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OfficerResponse(
        UUID officerId, String nationalId, String firstName, String lastName,
        String username, String port, OfficerRole role, OfficerStatus status, LocalDateTime createdAt
) {
    public static OfficerResponse from(CustomsOfficer o) {
        return new OfficerResponse(o.getOfficerId(), o.getNationalId(), o.getFirstName(), o.getLastName(),
                o.getUsername(), o.getPort(), o.getRole(), o.getStatus(), o.getCreatedAt());
    }
}
