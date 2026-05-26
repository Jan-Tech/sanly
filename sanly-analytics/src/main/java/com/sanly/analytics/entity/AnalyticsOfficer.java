package com.sanly.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analytics_officers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalyticsOfficer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "officer_id")
    private UUID officerId;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    @Builder.Default private String role = "ROLE_ADMIN";

    @Builder.Default private boolean active = true;

    @Column(name = "created_at")
    @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();
}
