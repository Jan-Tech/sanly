package com.sanly.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_exports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportExport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "export_id")
    private UUID exportId;

    @Column(name = "export_code", unique = true, nullable = false, length = 30)
    private String exportCode;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;

    @Column(name = "generated_by_officer_id")
    private UUID generatedByOfficerId;

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(nullable = false, length = 20)
    @Builder.Default private String status = "GENERATING";

    @Column(name = "report_data", columnDefinition = "BYTEA")
    private byte[] reportData;

    @Column(name = "report_data_csv", columnDefinition = "BYTEA")
    private byte[] reportDataCsv;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "generated_at")
    @Builder.Default private LocalDateTime generatedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
