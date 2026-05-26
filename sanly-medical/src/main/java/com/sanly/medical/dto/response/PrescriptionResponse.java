package com.sanly.medical.dto.response;

import com.sanly.medical.entity.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PrescriptionResponse {
    private UUID prescriptionId;
    private String prescriptionCode;
    private String citizenNationalId;
    private Long issuedByDoctorId;
    private Long clinicId;
    private String diagnosisCode;
    private String medicationName;
    private String medicationDosage;
    private MedicationForm medicationForm;
    private Integer quantity;
    private MedicationUnit unit;
    private Integer refillsAllowed;
    private Integer refillsUsed;
    private String instructions;
    private LocalDateTime issuedAt;
    private LocalDate expiresAt;
    private PrescriptionStatus status;

    public static PrescriptionResponse from(Prescription p) {
        return PrescriptionResponse.builder()
                .prescriptionId(p.getPrescriptionId())
                .prescriptionCode(p.getPrescriptionCode())
                .citizenNationalId(p.getCitizenNationalId())
                .issuedByDoctorId(p.getIssuedByDoctorId())
                .clinicId(p.getClinicId())
                .diagnosisCode(p.getDiagnosisCode())
                .medicationName(p.getMedicationName())
                .medicationDosage(p.getMedicationDosage())
                .medicationForm(p.getMedicationForm())
                .quantity(p.getQuantity())
                .unit(p.getUnit())
                .refillsAllowed(p.getRefillsAllowed())
                .refillsUsed(p.getRefillsUsed())
                .instructions(p.getInstructions())
                .issuedAt(p.getIssuedAt())
                .expiresAt(p.getExpiresAt())
                .status(p.getStatus())
                .build();
    }
}
