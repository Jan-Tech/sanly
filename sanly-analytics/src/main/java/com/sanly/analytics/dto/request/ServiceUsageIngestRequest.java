package com.sanly.analytics.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ServiceUsageIngestRequest(
    @NotBlank String serviceName,
    String endpoint,
    long requestCount,
    double avgResponseMs,
    long errorCount
) {}
