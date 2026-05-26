package com.sanly.court.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "courts")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class Court {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID courtId;

    @Column(nullable = false, unique = true)
    private String courtCode; // TM-CRT-NNN

    @Column(nullable = false) private String name;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private CourtType courtType;

    private String region;
    private String address;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private CourtStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = CourtStatus.ACTIVE;
    }
}
