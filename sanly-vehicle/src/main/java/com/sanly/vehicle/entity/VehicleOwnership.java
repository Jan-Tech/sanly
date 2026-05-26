package com.sanly.vehicle.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "vehicle_ownerships")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleOwnership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ownershipId;

    @Column(nullable = false, length = 12)
    private String plateNumber;

    @Column(length = 20)
    private String ownerNationalId;

    @Column(length = 20)
    private String ownerBusinessNumber;

    @Column(nullable = false)
    private LocalDate ownershipStartDate;

    private LocalDate ownershipEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcquiredVia acquiredVia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OwnershipStatus status;
}
