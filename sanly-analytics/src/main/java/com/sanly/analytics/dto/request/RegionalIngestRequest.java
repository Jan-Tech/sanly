package com.sanly.analytics.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegionalIngestRequest(
    @NotBlank String region,
    Long citizenCount,
    Long businessCount,
    Long propertyCount,
    Long appointmentCount,
    Long benefitClaimCount
) {}
