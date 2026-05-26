package com.sanly.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dispense_records",
    indexes = {
        @Index(name = "idx_dr_rx_code",  columnList = "prescription_code"),
        @Index(name = "idx_dr_citizen",  columnList = "citizen_national_id"),
        @Index(name = "idx_dr_staff",    columnList = "staff_id")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "record_id", updatable = false, nullable = false)
    private UUID recordId;

    @Column(name = "prescription_code", nullable = false, length = 20)
    private String prescriptionCode;

    @Column(name = "citizen_national_id", nullable = false, length = 11)
    private String citizenNationalId;

    @Column(name = "pharmacy_code", nullable = false, length = 20)
    private String pharmacyCode;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Builder.Default
    @Column(name = "dispensed_at", nullable = false)
    private LocalDateTime dispensedAt = LocalDateTime.now();

    @Column(name = "quantity_dispensed", nullable = false)
    private Integer quantityDispensed;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** True when the prescription was successfully verified through SANLY Bridge */
    @Builder.Default
    @Column(name = "bridge_verified", nullable = false)
    private boolean bridgeVerified = false;
}
