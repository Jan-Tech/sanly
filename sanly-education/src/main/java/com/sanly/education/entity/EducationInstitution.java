package com.sanly.education.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "education_institutions")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class EducationInstitution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID institutionId;

    @Column(nullable = false, unique = true)
    private String institutionCode; // TM-EDU-NNNN

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstitutionType type;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String address;

    private String licenseNumber;

    private LocalDate accreditedUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstitutionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = InstitutionStatus.ACTIVE;
    }
}
