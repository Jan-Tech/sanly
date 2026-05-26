package com.sanly.social.dto.response;

import com.sanly.social.entity.BenefitClaim;
import com.sanly.social.entity.ClaimStatus;
import com.sanly.social.entity.ClaimType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClaimResponse(
        UUID claimId,
        String claimCode,
        String citizenNationalId,
        String programCode,
        ClaimType claimType,
        ClaimStatus status,
        LocalDateTime appliedAt,
        LocalDateTime approvedAt,
        LocalDate expiresAt,
        String rejectionReason,
        String triggerReason,
        LocalDateTime createdAt
) {
    public static ClaimResponse from(BenefitClaim c) {
        return new ClaimResponse(c.getClaimId(), c.getClaimCode(), c.getCitizenNationalId(),
                c.getProgramCode(), c.getClaimType(), c.getStatus(), c.getAppliedAt(),
                c.getApprovedAt(), c.getExpiresAt(), c.getRejectionReason(),
                c.getTriggerReason(), c.getCreatedAt());
    }
}
