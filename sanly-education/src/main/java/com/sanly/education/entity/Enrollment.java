package com.sanly.education.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID enrollmentId;

    @Column(nullable = false)
    private String citizenNationalId;

    @Column(nullable = false)
    private String institutionCode;

    @Column(nullable = false)
    private LocalDate enrollmentDate;

    private Integer expectedGraduationYear;

    private String programName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = EnrollmentStatus.ACTIVE;
    }
}
