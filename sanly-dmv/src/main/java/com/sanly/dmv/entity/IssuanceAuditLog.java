package com.sanly.dmv.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Records every license issuance attempt — success or failure.
 * Persisted in a REQUIRES_NEW transaction so failures are captured
 * even when the outer issuance transaction rolls back.
 */
@Entity
@Table(name = "issuance_audit_log",
    indexes = {
        @Index(name = "idx_ial_citizen",   columnList = "citizen_national_id"),
        @Index(name = "idx_ial_outcome",   columnList = "outcome"),
        @Index(name = "idx_ial_processed", columnList = "processed_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssuanceAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "citizen_national_id", length = 30, nullable = false)
    private String citizenNationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 10, nullable = false)
    private LicenseCategory category;

    @Column(name = "processed_by_officer_id", nullable = false)
    private Long processedByOfficerId;

    /**
     * Outcome constants: SUCCESS / REJECTED_NO_VISION / REJECTED_EXPIRED /
     * REJECTED_FAIL / REJECTED_DUPLICATE / REJECTED_NOT_APPROVED.
     */
    @Column(name = "outcome", length = 30, nullable = false)
    private String outcome;

    @Column(name = "outcome_detail", columnDefinition = "TEXT")
    private String outcomeDetail;

    /** Bridge recordRef of the vision test found (may be null if not found). */
    @Column(name = "vision_test_ref", length = 500)
    private String visionTestRef;

    /** Populated only when outcome = SUCCESS. */
    @Column(name = "license_number", length = 20)
    private String licenseNumber;

    @Column(name = "processed_at", nullable = false)
    @Builder.Default
    private LocalDateTime processedAt = LocalDateTime.now();
}
