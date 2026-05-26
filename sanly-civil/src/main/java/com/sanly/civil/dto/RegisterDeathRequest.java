package com.sanly.civil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegisterDeathRequest(
        @NotBlank String deceasedNationalId,
        @NotBlank String deceasedFullName,
        @NotNull LocalDate dateOfDeath,
        @NotBlank String placeOfDeath,
        String deathCause
) {}
