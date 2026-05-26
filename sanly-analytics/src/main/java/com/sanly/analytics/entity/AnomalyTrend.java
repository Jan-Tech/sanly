package com.sanly.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "anomaly_trends",
    uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "institution_code", "alert_type"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnomalyTrend {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "trend_id")
    private UUID trendId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "institution_code", length = 50)
    private String institutionCode;

    @Column(name = "alert_type", length = 50)
    private String alertType;

    @Builder.Default private long alertCount = 0;
    @Builder.Default private long resolvedCount = 0;
    @Builder.Default private double avgResolutionHours = 0.0;

    @Column(name = "created_at")
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
