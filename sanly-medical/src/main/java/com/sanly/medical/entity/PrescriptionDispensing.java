package com.sanly.medical.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescription_dispensings",
    indexes = {
        @Index(name = "idx_disp_rx_code",  columnList = "prescription_code"),
        @Index(name = "idx_disp_pharmacy", columnList = "dispensed_by_pharmacy_code")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDispensing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "dispensing_id", updatable = false, nullable = false)
    private UUID dispensingId;

    @Column(name = "prescription_code", nullable = false, length = 20)
    private String prescriptionCode;

    @Column(name = "dispensed_by_pharmacy_code", nullable = false, length = 20)
    private String dispensedByPharmacyCode;

    @Builder.Default
    @Column(name = "dispensed_at", nullable = false)
    private LocalDateTime dispensedAt = LocalDateTime.now();

    @Column(name = "quantity_dispensed", nullable = false)
    private Integer quantityDispensed;

    @Column(name = "pharmacist_national_id", length = 11)
    private String pharmacistNationalId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
