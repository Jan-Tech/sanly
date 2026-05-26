package com.sanly.civil.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pending_enrollments",
    indexes = {
        @Index(name = "idx_pe_child",        columnList = "child_national_id"),
        @Index(name = "idx_pe_school_year",  columnList = "expected_school_year"),
        @Index(name = "idx_pe_status",       columnList = "status")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "enrollment_id", updatable = false, nullable = false)
    private UUID enrollmentId;

    @Column(name = "child_national_id", nullable = false, length = 11)
    private String childNationalId;

    @Column(name = "mother_national_id", length = 11)
    private String motherNationalId;

    @Column(name = "father_national_id", length = 11)
    private String fatherNationalId;

    @Column(name = "child_full_name", length = 300)
    private String childFullName;

    /** The year the child should start school (birthYear + 6) */
    @Column(name = "expected_school_year", nullable = false)
    private Integer expectedSchoolYear;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 15)
    private EnrollmentStatus status = EnrollmentStatus.QUEUED;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;
}
