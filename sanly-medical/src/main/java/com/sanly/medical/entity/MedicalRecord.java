package com.sanly.medical.entity;

import com.sanly.medical.config.EncryptedStringConverter;
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
@Table(name = "medical_records",
    indexes = {
        @Index(name = "idx_mr_citizen",        columnList = "citizen_national_id"),
        @Index(name = "idx_mr_clinic_id",      columnList = "clinic_id"),
        @Index(name = "idx_mr_doctor_id",      columnList = "doctor_id"),
        @Index(name = "idx_mr_test_type",      columnList = "test_type"),
        @Index(name = "idx_mr_citizen_clinic", columnList = "citizen_national_id, clinic_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID recordId;

    /** Reference to citizen-registry — no FK across services. */
    @Column(name = "citizen_national_id", length = 30, nullable = false)
    private String citizenNationalId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", length = 30, nullable = false)
    private TestType testType;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 10, nullable = false)
    @Builder.Default
    private RecordResult result = RecordResult.PENDING;

    /** AES-256-GCM encrypted — may contain sensitive clinical notes. */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "tested_at", nullable = false)
    private LocalDateTime testedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** Set to true after successful async publish to SANLY Bridge. */
    @Column(name = "bridge_published", nullable = false)
    @Builder.Default
    private boolean bridgePublished = false;

    @Column(name = "bridge_published_at")
    private LocalDateTime bridgePublishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
