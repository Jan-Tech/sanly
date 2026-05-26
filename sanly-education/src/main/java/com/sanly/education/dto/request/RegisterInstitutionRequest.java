package com.sanly.education.dto.request;

import com.sanly.education.entity.InstitutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegisterInstitutionRequest(
        @NotBlank String name,
        @NotNull InstitutionType type,
        @NotBlank String region,
        @NotBlank String address,
        String licenseNumber,
        LocalDate accreditedUntil
) {}
