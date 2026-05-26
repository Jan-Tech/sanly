package com.sanly.education.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "diplomas")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class Diploma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID diplomaId;

    @Column(nullable = false, unique = true)
    private String diplomaCode; // TM-DIP-YYYYNNNNNN

    @Column(nullable = false)
    private String citizenNationalId;

    @Column(nullable = false)
    private String institutionCode;

    @Column(nullable = false)
    private String programName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgramLevel programLevel;

    @Column(nullable = false)
    private LocalDate graduationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Honors honors;

    private UUID issuedByOfficerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiplomaStatus status;

    private String revokedReason;

    private boolean bridgePublished;

    @PrePersist
    void prePersist() {
        issuedAt = LocalDateTime.now();
        if (status == null) status = DiplomaStatus.VALID;
        if (honors == null) honors = Honors.NONE;
    }
}
