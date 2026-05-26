package com.sanly.civil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegisterMarriageRequest(
        @NotBlank String spouse1NationalId,
        @NotBlank String spouse1FullName,
        @NotBlank String spouse2NationalId,
        @NotBlank String spouse2FullName,
        @NotNull LocalDate marriageDate
) {}
