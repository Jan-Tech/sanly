package com.sanly.education.dto.request;

import com.sanly.education.entity.Honors;
import com.sanly.education.entity.ProgramLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record IssueDiplomaRequest(
        @NotBlank String citizenNationalId,
        @NotBlank String institutionCode,
        @NotBlank String programName,
        @NotNull ProgramLevel programLevel,
        @NotNull LocalDate graduationDate,
        Honors honors
) {}
