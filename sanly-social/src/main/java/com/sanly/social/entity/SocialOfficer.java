package com.sanly.social.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "social_officers")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class SocialOfficer {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID officerId;

    @Column(nullable = false, unique = true) private String nationalId;
    @Column(nullable = false) private String firstName;
    @Column(nullable = false) private String lastName;
    @Column(nullable = false, unique = true) private String username;
    @Column(nullable = false) private String password;
    private String region;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private OfficerRole role;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private OfficerStatus status;

    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = OfficerStatus.ACTIVE;
    }
}
