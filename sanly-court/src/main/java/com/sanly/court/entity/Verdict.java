package com.sanly.court.entity;

import com.sanly.court.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verdicts")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class Verdict {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID verdictId;

    @Column(nullable = false, unique = true)
    private String caseNumber;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private VerdictType verdictType;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String summary;

    private UUID issuedByJudgeOfficerId;

    @Column(nullable = false) private LocalDateTime issuedAt;
    @Column(nullable = false) private LocalDate appealDeadline; // issuedAt + 30 days

    @Column(nullable = false) private boolean appealed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        if (issuedAt == null) issuedAt = createdAt;
        if (appealDeadline == null) appealDeadline = issuedAt.toLocalDate().plusDays(30);
    }
}
