package com.sanly.court.entity;

import com.sanly.court.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "court_cases")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class CourtCase {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID caseId;

    @Column(nullable = false, unique = true)
    private String caseNumber; // TM-CASE-YYYYNNNNNN

    @Column(nullable = false) private String courtCode;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private CaseType caseType;

    @Column(nullable = false) private String plaintiffNationalId;
    @Column(nullable = false) private String defendantNationalId;
    private UUID assignedJudgeOfficerId;

    @Column(nullable = false) private LocalDateTime filedAt;
    private LocalDate hearingDate;
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private CaseStatus status;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String summary;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (filedAt == null) filedAt = createdAt;
        if (status == null) status = CaseStatus.FILED;
    }

    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
}
