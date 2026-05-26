package com.sanly.education.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddAcademicRecordRequest(
        @NotBlank String citizenNationalId,
        @NotBlank String institutionCode,
        @NotBlank String academicYear,
        String grade,
        String gpa,
        String notes
) {}
