package com.sanly.dmv.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "driving_licenses",
    indexes = {
        @Index(name = "idx_lic_citizen",     columnList = "citizen_national_id"),
        @Index(name = "idx_lic_number",      columnList = "license_number"),
        @Index(name = "idx_lic_status",      columnList = "status"),
        @Index(name = "idx_lic_expires",     columnList = "expires_at"),
        @Index(name = "idx_lic_citizen_cat", columnList = "citizen_national_id, category, status")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrivingLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID licenseId;

    @Column(name = "citizen_national_id", length = 30, nullable = false)
    private String citizenNationalId;

    @Column(name = "license_number", length = 20, nullable = false, unique = true)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 10, nullable = false)
    private LicenseCategory category;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "issued_by_officer_id", nullable = false)
    private Long issuedByOfficerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    @Builder.Default
    private LicenseStatus status = LicenseStatus.ACTIVE;

    /** Bridge recordRef of the vision test used to issue this license. */
    @Column(name = "vision_test_ref", length = 500)
    private String visionTestRef;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
