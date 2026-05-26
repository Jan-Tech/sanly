package com.sanly.civil.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "civil_officers")
@Getter
@Setter
public class CivilOfficer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    private String nationalId;
    private String officeRegion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfficerStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "officer_roles", joinColumns = @JoinColumn(name = "officer_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @CreationTimestamp
    private Instant createdAt;
}
