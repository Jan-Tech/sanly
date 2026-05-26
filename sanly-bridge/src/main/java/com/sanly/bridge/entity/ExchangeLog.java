package com.sanly.bridge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Immutable record of every data exchange routed through SANLY Bridge.
 * Never updated or deleted — forms the complete audit trail.
 */
@Entity
@Table(name = "exchange_logs",
    indexes = {
        @Index(name = "idx_exlog_requesting",   columnList = "requesting_code"),
        @Index(name = "idx_exlog_national_id",  columnList = "national_id"),
        @Index(name = "idx_exlog_data_type",    columnList = "data_type"),
        @Index(name = "idx_exlog_result",       columnList = "result"),
        @Index(name = "idx_exlog_exchanged_at", columnList = "exchanged_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requesting_code", length = 50, nullable = false)
    private String requestingCode;

    /** Null for PUBLISH operations (no target — data flows into the bridge). */
    @Column(name = "target_code", length = 50)
    private String targetCode;

    @Column(name = "national_id", length = 30)
    private String nationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 50, nullable = false)
    private DataType dataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", length = 10, nullable = false)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 15, nullable = false)
    private ExchangeResult result;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    /** Denial reason, error message, or other contextual detail. */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "exchanged_at", nullable = false)
    private LocalDateTime exchangedAt;

    // ── Purpose Code fields (Anti-Corruption Feature 1) ──────────────────────

    /** Mandatory declared reason for every QUERY. Null only for PUBLISH operations. */
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose_code", length = 50)
    private QueryPurpose purposeCode;

    /**
     * Court order number, case number, traffic stop ID, etc.
     * Required for CRIMINAL_RECORD and MEDICAL_CLEARANCE queries.
     * Visible to citizens in their own access log.
     */
    @Column(name = "case_reference", length = 100)
    private String caseReference;

    /** Optional free-text justification (max 500 chars). */
    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;
}
