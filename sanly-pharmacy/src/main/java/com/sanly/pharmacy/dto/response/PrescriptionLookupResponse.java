package com.sanly.pharmacy.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * View returned to pharmacy staff after looking up a prescription via the bridge.
 * Contains details safe to show pharmacist — no encrypted fields.
 */
@Data
@Builder
public class PrescriptionLookupResponse {
    private String prescriptionCode;
    private String citizenNationalId;
    private String citizenFullName;
    private String medicationName;
    private String medicationDosage;
    private String medicationForm;
    private Integer quantity;
    private String unit;
    private Integer refillsAllowed;
    private Integer refillsUsed;
    private Integer refillsRemaining;
    private String instructions;
    private String issuedAt;
    private LocalDate expiresAt;
    private String status;
    private boolean expired;
}
