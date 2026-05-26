package com.sanly.court.entity;

import com.sanly.court.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "court_fines")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class CourtFine {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID fineId;

    @Column(nullable = false, unique = true)
    private String fineCode; // TM-FINE-YYYYNNNNNN

    @Column(nullable = false) private String caseNumber;
    @Column(nullable = false) private String citizenNationalId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String amount;

    @Column(nullable = false) private String reason;

    @Column(nullable = false) private LocalDateTime issuedAt;
    @Column(nullable = false) private LocalDate dueDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private FineStatus status;

    private LocalDateTime paidAt;

    @Convert(converter = EncryptedStringConverter.class)
    private String paymentProofNote;

    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (issuedAt == null) issuedAt = createdAt;
        if (status == null) status = FineStatus.OUTSTANDING;
    }

    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
}
