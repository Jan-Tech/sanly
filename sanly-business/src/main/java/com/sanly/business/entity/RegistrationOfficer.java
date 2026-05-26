package com.sanly.business.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "registration_officers")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RegistrationOfficer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long officerId;

    @Column(name = "national_id", length = 11, unique = true)
    private String nationalId;

    @Column(name = "first_name", length = 150, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 150, nullable = false)
    private String lastName;

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "officer_roles", joinColumns = @JoinColumn(name = "officer_id"))
    @Column(name = "role", length = 30)
    private Set<String> roles;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OfficerStatus status = OfficerStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
