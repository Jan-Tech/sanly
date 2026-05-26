package com.sanly.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "regional_stats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "region"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegionalStat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "stat_id")
    private UUID statId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(nullable = false, length = 100)
    private String region;

    @Builder.Default private long citizenCount = 0;
    @Builder.Default private long businessCount = 0;
    @Builder.Default private long propertyCount = 0;
    @Builder.Default private long appointmentCount = 0;
    @Builder.Default private long benefitClaimCount = 0;

    @Column(name = "created_at")
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
