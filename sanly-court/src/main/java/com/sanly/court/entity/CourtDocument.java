package com.sanly.court.entity;

import com.sanly.court.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "court_documents")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class CourtDocument {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID documentId;

    @Column(nullable = false, unique = true)
    private String documentCode; // TM-DOC-YYYYNNNNNN

    @Column(nullable = false) private String caseNumber;
    @Column(nullable = false) private String title;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private DocumentType documentType;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false) private String submittedByNationalId;

    @Column(nullable = false) private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private DocumentStatus status;

    @Column(nullable = false, length = 64)
    private String digitalSignature; // SHA-256 hex

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        if (submittedAt == null) submittedAt = createdAt;
        if (status == null) status = DocumentStatus.SUBMITTED;
    }
}
