package com.sanly.tax.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "child_benefit_records",
    indexes = {
        @Index(name = "idx_cbr_child",  columnList = "child_national_id"),
        @Index(name = "idx_cbr_mother", columnList = "mother_national_id"),
        @Index(name = "idx_cbr_father", columnList = "father_national_id"),
        @Index(name = "idx_cbr_status", columnList = "status")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildBenefitRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "benefit_id", updatable = false, nullable = false)
    private UUID benefitId;

    @Column(name = "child_national_id", nullable = false, length = 11)
    private String childNationalId;

    @Column(name = "mother_national_id", length = 11)
    private String motherNationalId;

    @Column(name = "father_national_id", length = 11)
    private String fatherNationalId;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 15)
    private BenefitStatus status = BenefitStatus.ACTIVE;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
