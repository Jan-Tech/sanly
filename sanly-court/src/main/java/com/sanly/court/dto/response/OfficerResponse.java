package com.sanly.court.dto.response;

import com.sanly.court.entity.CourtOfficer;
import com.sanly.court.entity.OfficerRole;
import com.sanly.court.entity.OfficerStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OfficerResponse(
        UUID officerId, String nationalId, String firstName, String lastName,
        String username, String courtCode, OfficerRole role, OfficerStatus status, LocalDateTime createdAt
) {
    public static OfficerResponse from(CourtOfficer o) {
        return new OfficerResponse(o.getOfficerId(), o.getNationalId(), o.getFirstName(), o.getLastName(),
                o.getUsername(), o.getCourtCode(), o.getRole(), o.getStatus(), o.getCreatedAt());
    }
}
