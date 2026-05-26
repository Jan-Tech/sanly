package com.sanly.analytics.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AnomalyIngestRequest(
    @NotBlank String institutionCode,
    @NotBlank String alertType,
    long alertCount,
    long resolvedCount,
    double avgResolutionHours
) {}
