package com.sanly.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_snapshots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "snapshot_id")
    private UUID snapshotId;

    @Column(name = "snapshot_date", unique = true, nullable = false)
    private LocalDate snapshotDate;

    // Citizens
    @Builder.Default private long totalCitizens = 0;
    @Builder.Default private long activeCitizens = 0;
    @Builder.Default private long deceasedCitizens = 0;
    @Builder.Default private long newRegistrationsToday = 0;

    // Businesses
    @Builder.Default private long totalBusinesses = 0;
    @Builder.Default private long activeBusinesses = 0;
    @Builder.Default private long newBusinessesToday = 0;

    // Licenses
    @Builder.Default private long totalLicenses = 0;
    @Builder.Default private long licensesIssuedToday = 0;

    // Diplomas
    @Builder.Default private long totalDiplomas = 0;
    @Builder.Default private long diplomasIssuedToday = 0;

    // Properties
    @Builder.Default private long totalProperties = 0;
    @Builder.Default private long transfersToday = 0;

    // Benefits
    @Builder.Default private long totalBenefitClaims = 0;
    @Builder.Default private long activeClaimants = 0;

    // Court
    @Builder.Default private long totalCourtCases = 0;
    @Builder.Default private long openCases = 0;

    // Customs
    @Column(name = "total_customs_decls")
    @Builder.Default private long totalCustomsDeclarations = 0;
    @Builder.Default private long clearancesToday = 0;

    // Appointments
    @Builder.Default private long totalAppointments = 0;
    @Builder.Default private long appointmentsToday = 0;
    @Builder.Default private long noShowCount = 0;
    @Builder.Default private double avgAppointmentRating = 0.0;

    // Bridge
    @Builder.Default private long totalBridgeExchanges = 0;
    @Builder.Default private long exchangesToday = 0;

    // Anomalies
    @Builder.Default private long totalAnomalyAlerts = 0;
    @Builder.Default private long openAnomalyAlerts = 0;

    @Column(name = "created_at")
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
