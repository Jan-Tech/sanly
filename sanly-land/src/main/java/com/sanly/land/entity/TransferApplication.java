package com.sanly.land.entity;

import com.sanly.land.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfer_applications")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class TransferApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID applicationId;

    @Column(nullable = false)
    private String cadastralNumber;

    @Column(nullable = false)
    private String fromNationalId;

    @Column(nullable = false)
    private String toNationalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferType transferType;

    @Convert(converter = EncryptedStringConverter.class)
    private String agreedPrice; // encrypted, nullable (not required for gifts/inheritance)

    @Column(nullable = false)
    private LocalDate applicationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    private UUID processedByOfficerId;

    private LocalDateTime processedAt;

    private String rejectionReason;

    @Convert(converter = EncryptedStringConverter.class)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = TransferStatus.PENDING;
    }
}
