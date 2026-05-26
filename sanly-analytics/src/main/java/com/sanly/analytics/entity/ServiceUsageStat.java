package com.sanly.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_usage_stats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "service_name", "endpoint"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceUsageStat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "stat_id")
    private UUID statId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "service_name", nullable = false, length = 50)
    private String serviceName;

    @Column(length = 200)
    private String endpoint;

    @Builder.Default private long requestCount = 0;
    @Builder.Default private double avgResponseMs = 0.0;
    @Builder.Default private long errorCount = 0;
    @Builder.Default private double errorRate = 0.0;

    @Column(name = "created_at")
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
