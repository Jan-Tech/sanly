package com.sanly.vehicle.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_officers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleOfficer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long officerId;

    @Column(nullable = false, length = 20)
    private String nationalId;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfficerRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfficerStatus status;
}
