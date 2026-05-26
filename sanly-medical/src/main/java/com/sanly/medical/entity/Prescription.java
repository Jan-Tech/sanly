package com.sanly.medical.entity;

import com.sanly.medical.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescriptions",
    indexes = {
        @Index(name = "idx_rx_code",   columnList = "prescription_code", unique = true),
        @Index(name = "idx_rx_nin",    columnList = "citizen_national_id"),
        @Index(name = "idx_rx_doctor", columnList = "issued_by_doctor_id"),
        @Index(name = "idx_rx_status", columnList = "status")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "prescription_id", updatable = false, nullable = false)
    private UUID prescriptionId;

    /** Human-readable code pharmacies scan: TM-RX-YYYYNNNNNN */
    @Column(name = "prescription_code", nullable = false, unique = true, length = 20)
    private String prescriptionCode;

    @Column(name = "citizen_national_id", nullable = false, length = 11)
    private String citizenNationalId;

    @Column(name = "issued_by_doctor_id", nullable = false)
    private Long issuedByDoctorId;

    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    /** ICD-10 diagnosis code — encrypted at rest */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "diagnosis_code", nullable = false)
    private String diagnosisCode;

    @Column(name = "medication_name", nullable = false, length = 200)
    private String medicationName;

    @Column(name = "medication_dosage", nullable = false, length = 100)
    private String medicationDosage;

    @Enumerated(EnumType.STRING)
    @Column(name = "medication_form", nullable = false, length = 20)
    private MedicationForm medicationForm;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 10)
    private MedicationUnit unit;

    @Builder.Default
    @Column(name = "refills_allowed", nullable = false)
    private Integer refillsAllowed = 0;

    @Builder.Default
    @Column(name = "refills_used", nullable = false)
    private Integer refillsUsed = 0;

    /** Dispensing instructions — encrypted at rest */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Builder.Default
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    /** Default 30 days from issuance */
    @Column(name = "expires_at", nullable = false)
    private LocalDate expiresAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 25)
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;

    @Builder.Default
    @Column(name = "bridge_published", nullable = false)
    private boolean bridgePublished = false;
}
