package com.sanly.social.dto.response;

import com.sanly.social.entity.PensionStatus;

import java.time.LocalDate;

public record PensionEligibilityResponse(
        boolean eligible,
        LocalDate eligibleAt,
        long yearsRemaining,
        PensionStatus status
) {}
