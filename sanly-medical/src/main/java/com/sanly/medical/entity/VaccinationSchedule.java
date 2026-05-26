package com.sanly.medical.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vaccination_schedules",
    indexes = {
        @Index(name = "idx_vs_citizen", columnList = "citizen_national_id", unique = true)
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "schedule_id", updatable = false, nullable = false)
    private UUID scheduleId;

    @Column(name = "citizen_national_id", nullable = false, unique = true, length = 11)
    private String citizenNationalId;

    /** Stored so the daily reminder job can notify parents without a cross-service call */
    @Column(name = "mother_national_id", length = 11)
    private String motherNationalId;

    @Column(name = "father_national_id", length = 11)
    private String fatherNationalId;

    @Builder.Default
    @Column(name = "schedule_status", nullable = false, length = 15)
    private String scheduleStatus = "ACTIVE";

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
