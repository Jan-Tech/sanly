package com.sanly.police.entity;

import com.sanly.police.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "criminal_records",
    indexes = {
        @Index(name = "idx_cr_citizen",    columnList = "citizen_national_id"),
        @Index(name = "idx_cr_verdict",    columnList = "verdict"),
        @Index(name = "idx_cr_status",     columnList = "status")
    })
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CriminalRecord {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID recordId;

    @Column(name = "citizen_national_id", length = 30, nullable = false)
    private String citizenNationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "offense_type", length = 30, nullable = false)
    private OffenseType offenseType;

    @Column(name = "offense_date", nullable = false)
    private LocalDate offenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", length = 20, nullable = false)
    private Verdict verdict;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "sentence_description", columnDefinition = "TEXT")
    private String sentenceDescription;

    @Column(name = "court_name", length = 200)
    private String courtName;

    @Column(name = "recorded_by_officer_id", nullable = false)
    private Long recordedByOfficerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "bridge_published", nullable = false)
    @Builder.Default
    private boolean bridgePublished = false;
}
