package com.sanly.social.dto.response;

import com.sanly.social.entity.PensionAccount;
import com.sanly.social.entity.PensionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PensionResponse(
        UUID accountId,
        String citizenNationalId,
        LocalDate contributionStartDate,
        String totalContributions,
        LocalDate eligibleAt,
        PensionStatus status,
        LocalDateTime createdAt
) {
    public static PensionResponse from(PensionAccount a) {
        return new PensionResponse(a.getAccountId(), a.getCitizenNationalId(),
                a.getContributionStartDate(), a.getTotalContributions(),
                a.getEligibleAt(), a.getStatus(), a.getCreatedAt());
    }
}
