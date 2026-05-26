package com.sanly.registry.entity;

import com.sanly.registry.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "data_deletion_requests",
    indexes = {
        @Index(name = "idx_ddr_national_id", columnList = "citizen_national_id"),
        @Index(name = "idx_ddr_status",      columnList = "status"),
        @Index(name = "idx_ddr_type",        columnList = "request_type")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataDeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_id", updatable = false, nullable = false)
    private UUID requestId;

    @Column(name = "request_code", unique = true, length = 22, nullable = false)
    private String requestCode;

    @Column(name = "citizen_national_id", length = 11, nullable = false)
    private String citizenNationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 30)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "affected_service", nullable = false, length = 25)
    private AffectedService affectedService;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by_officer_id", length = 100)
    private String reviewedByOfficerId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "reviewer_notes", columnDefinition = "TEXT")
    private String reviewerNotes;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "resolution_description", columnDefinition = "TEXT")
    private String resolutionDescription;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "export_data", columnDefinition = "TEXT")
    private String exportData;
}
