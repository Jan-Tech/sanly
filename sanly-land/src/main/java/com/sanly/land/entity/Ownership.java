package com.sanly.land.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ownerships")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class Ownership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID ownershipId;

    @Column(nullable = false)
    private String cadastralNumber;

    @Column(nullable = false)
    private String ownerNationalId;

    // Percentage 1-100; multiple owners must sum to 100
    @Column(nullable = false)
    private Integer ownershipShare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnershipType ownershipType;

    @Column(nullable = false)
    private LocalDate acquiredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcquiredVia acquiredVia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnershipStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = OwnershipStatus.ACTIVE;
    }
}
