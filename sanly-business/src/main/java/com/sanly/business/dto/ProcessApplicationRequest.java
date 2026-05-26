package com.sanly.business.dto;

import jakarta.validation.constraints.NotNull;

public record ProcessApplicationRequest(
        @NotNull Boolean approve,
        String rejectionReason
) {}
