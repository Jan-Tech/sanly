package com.sanly.social.dto.response;

import com.sanly.social.entity.OfficerRole;
import com.sanly.social.entity.OfficerStatus;
import com.sanly.social.entity.SocialOfficer;

import java.time.LocalDateTime;
import java.util.UUID;

public record OfficerResponse(
        UUID officerId,
        String nationalId,
        String username,
        String region,
        OfficerRole role,
        OfficerStatus status,
        LocalDateTime createdAt
) {
    public static OfficerResponse from(SocialOfficer o) {
        return new OfficerResponse(o.getOfficerId(), o.getNationalId(), o.getUsername(),
                o.getRegion(), o.getRole(), o.getStatus(), o.getCreatedAt());
    }
}
