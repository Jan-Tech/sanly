package com.sanly.land.dto.response;

import com.sanly.land.entity.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record OfficerResponse(
        UUID officerId,
        String nationalId,
        String firstName,
        String lastName,
        String username,
        String region,
        OfficerRole role,
        OfficerStatus status,
        LocalDateTime createdAt
) {
    public static OfficerResponse from(LandOfficer o) {
        return new OfficerResponse(o.getOfficerId(), o.getNationalId(), o.getFirstName(),
                o.getLastName(), o.getUsername(), o.getRegion(), o.getRole(),
                o.getStatus(), o.getCreatedAt());
    }
}
