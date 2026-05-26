package com.sanly.land.dto.request;

import com.sanly.land.entity.OfficerRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOfficerRequest(
        @NotBlank String nationalId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String username,
        @NotBlank String password,
        String region,
        @NotNull OfficerRole role
) {}
