package com.sanly.education.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EnrollRequest(
        @NotBlank String citizenNationalId,
        @NotBlank String institutionCode,
        @NotNull LocalDate enrollmentDate,
        Integer expectedGraduationYear,
        String programName
) {}
