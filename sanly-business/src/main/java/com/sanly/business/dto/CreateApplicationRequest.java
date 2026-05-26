package com.sanly.business.dto;

import com.sanly.business.entity.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateApplicationRequest(
        @NotBlank String applicantNationalId,
        @NotBlank String applicantFullName,
        @NotBlank String proposedBusinessName,
        @NotNull BusinessType businessType,
        @NotBlank String proposedAddress
) {}
