package com.sanly.medical.dto.request;

import com.sanly.medical.entity.MedicationForm;
import com.sanly.medical.entity.MedicationUnit;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class IssuePrescriptionRequest {

    @NotBlank(message = "citizenNationalId is required")
    @Size(min = 11, max = 11, message = "National ID must be exactly 11 digits")
    private String citizenNationalId;

    @NotBlank(message = "diagnosisCode is required")
    @Size(max = 20, message = "diagnosisCode max 20 chars")
    private String diagnosisCode;

    @NotBlank(message = "medicationName is required")
    @Size(max = 200)
    private String medicationName;

    @NotBlank(message = "medicationDosage is required")
    @Size(max = 100)
    private String medicationDosage;

    @NotNull(message = "medicationForm is required")
    private MedicationForm medicationForm;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be >= 1")
    private Integer quantity;

    @NotNull(message = "unit is required")
    private MedicationUnit unit;

    @Min(value = 0, message = "refillsAllowed must be 0–5")
    @Max(value = 5, message = "refillsAllowed must be 0–5")
    private int refillsAllowed = 0;

    @Size(max = 1000)
    private String instructions;

    /** Optional override — defaults to 30 days from now in service layer */
    private LocalDate expiresAt;
}
