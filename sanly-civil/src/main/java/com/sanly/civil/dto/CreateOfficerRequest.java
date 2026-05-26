package com.sanly.civil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOfficerRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String fullName,
        String nationalId,
        String officeRegion
) {}
