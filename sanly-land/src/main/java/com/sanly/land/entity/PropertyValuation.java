package com.sanly.land.entity;

import com.sanly.land.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "property_valuations")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class PropertyValuation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID valuationId;

    @Column(nullable = false)
    private String cadastralNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String valuationAmount; // encrypted

    @Column(nullable = false)
    private LocalDate valuationDate;

    private UUID valuedByOfficerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValuationPurpose purpose;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
