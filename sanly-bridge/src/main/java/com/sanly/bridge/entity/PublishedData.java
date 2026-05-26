package com.sanly.bridge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A data record published by an institution into SANLY Bridge.
 *
 * The bridge stores ONLY non-sensitive metadata — the {@code summary} is a small
 * JSON object (e.g. {"passed": true, "date": "2024-01-15"}).  Raw PII such as
 * full medical records stays in the source institution's own database;
 * {@code recordRef} is the reference number used to retrieve it from there.
 */
@Entity
@Table(name = "published_data",
    indexes = {
        @Index(name = "idx_pub_national_id",  columnList = "national_id"),
        @Index(name = "idx_pub_data_type",    columnList = "data_type"),
        @Index(name = "idx_pub_publisher",    columnList = "publisher_code"),
        @Index(name = "idx_pub_active",       columnList = "active"),
        @Index(name = "idx_pub_national_type",columnList = "national_id, data_type")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishedData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "publisher_code", length = 50, nullable = false)
    private String publisherCode;

    @Column(name = "national_id", length = 30, nullable = false)
    private String nationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 50, nullable = false)
    private DataType dataType;

    /** External record ID / URL in the publisher institution's own system. */
    @Column(name = "record_ref", length = 500)
    private String recordRef;

    /** Non-sensitive JSON metadata. Never raw PII. */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
