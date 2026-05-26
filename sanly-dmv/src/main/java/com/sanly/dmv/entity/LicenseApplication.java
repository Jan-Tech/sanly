package com.sanly.dmv.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "license_applications",
    indexes = {
        @Index(name = "idx_app_citizen",    columnList = "citizen_national_id"),
        @Index(name = "idx_app_status",     columnList = "status"),
        @Index(name = "idx_app_applied_at", columnList = "applied_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID applicationId;

    @Column(name = "citizen_national_id", length = 30, nullable = false)
    private String citizenNationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_category", length = 10, nullable = false)
    private LicenseCategory requestedCategory;

    @Column(name = "applied_at", nullable = false)
    @Builder.Default
    private LocalDateTime appliedAt = LocalDateTime.now();

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
}
