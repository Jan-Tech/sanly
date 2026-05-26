package com.sanly.business.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "registration_applications",
    indexes = {
        @Index(name = "idx_app_owner",  columnList = "owner_national_id"),
        @Index(name = "idx_app_status", columnList = "status")
    })
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RegistrationApplication {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID applicationId;

    @Column(name = "business_name", length = 300, nullable = false)
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", length = 25, nullable = false)
    private BusinessType businessType;

    @Column(name = "owner_national_id", length = 30, nullable = false)
    private String ownerNationalId;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "processed_by_officer_id")
    private Long processedByOfficerId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /** JSON array of bridge records for TAX_STATUS query result. */
    @Column(name = "tax_check_result", columnDefinition = "TEXT")
    private String taxCheckResult;

    /** JSON array of bridge records for CRIMINAL_RECORD query result. */
    @Column(name = "criminal_check_result", columnDefinition = "TEXT")
    private String criminalCheckResult;

    @Column(name = "tax_check_at")
    private LocalDateTime taxCheckAt;

    @Column(name = "criminal_check_at")
    private LocalDateTime criminalCheckAt;
}
