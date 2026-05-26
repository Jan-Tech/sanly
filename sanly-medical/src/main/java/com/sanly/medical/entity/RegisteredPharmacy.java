package com.sanly.medical.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registered_pharmacies",
    indexes = {
        @Index(name = "idx_pharmacy_code", columnList = "pharmacy_code", unique = true)
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredPharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pharmacy_id")
    private Long pharmacyId;

    /** Format: TM-PHR-NNNN */
    @Column(name = "pharmacy_code", nullable = false, unique = true, length = 20)
    private String pharmacyCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "license_number", nullable = false, length = 100, unique = true)
    private String licenseNumber;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /** BCrypt hash of the raw API key returned once at registration */
    @Column(name = "api_key_hash", nullable = false)
    private String apiKeyHash;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 15)
    private PharmacyStatus status = PharmacyStatus.ACTIVE;

    @Builder.Default
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();
}
