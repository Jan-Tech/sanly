package com.sanly.civil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegisterBirthRequest(
        @NotBlank String childNationalId,
        @NotBlank String childFirstName,
        @NotBlank String childLastName,
        @NotNull LocalDate dateOfBirth,
        @NotBlank String placeOfBirth,
        String fatherNationalId,
        String fatherFullName,
        String motherNationalId,
        String motherFullName
) {}
