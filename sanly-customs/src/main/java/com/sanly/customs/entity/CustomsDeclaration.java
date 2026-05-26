package com.sanly.customs.entity;

import com.sanly.customs.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customs_declarations")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class CustomsDeclaration {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID declarationId;

    @Column(nullable = false, unique = true)
    private String declarationCode; // TM-CUS-YYYYNNNNNN

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private DeclarantType declarantType;

    private String declarantNationalId;
    private String declarantBusinessNumber;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private DeclarationType declarationType;

    @Column(nullable = false) private String portCode;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String cargoDescription;

    private String hsCode;
    private String countryOfOrigin;
    private String countryOfDestination;
    private String quantity;

    @Enumerated(EnumType.STRING)
    private CargoUnit unit;

    @Convert(converter = EncryptedStringConverter.class)
    private String declaredValue;

    private String currency;

    @Convert(converter = EncryptedStringConverter.class)
    private String dutiesOwed;

    @Convert(converter = EncryptedStringConverter.class)
    private String dutiesPaid;

    @Column(nullable = false)
    private LocalDate declarationDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private DeclarationStatus status;

    private UUID processedByOfficerId;
    private LocalDateTime processedAt;
    private String rejectionReason;

    @Convert(converter = EncryptedStringConverter.class)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) status = DeclarationStatus.DRAFT;
        if (declarationDate == null) declarationDate = LocalDate.now();
        if (dutiesPaid == null) dutiesPaid = "0";
    }

    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
}
