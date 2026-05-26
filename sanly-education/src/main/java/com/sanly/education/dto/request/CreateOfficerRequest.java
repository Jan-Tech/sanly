package com.sanly.education.dto.request;

import com.sanly.education.entity.OfficerRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOfficerRequest(
        @NotBlank String nationalId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String username,
        @NotBlank String password,
        String institutionCode,
        @NotNull OfficerRole role
) {}
