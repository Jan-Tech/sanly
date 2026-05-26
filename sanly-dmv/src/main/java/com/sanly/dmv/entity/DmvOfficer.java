package com.sanly.dmv.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents both regular officers (ROLE_OFFICER) and admin accounts (ROLE_ADMIN).
 * Admin accounts have no nationalId or officeRegion.
 */
@Entity
@Table(name = "dmv_officers",
    indexes = {
        @Index(name = "idx_officer_status", columnList = "status"),
        @Index(name = "idx_officer_region", columnList = "office_region")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmvOfficer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long officerId;

    /** The officer's own TM-NIN. Null for admin accounts. */
    @Column(name = "national_id", length = 11, unique = true)
    private String nationalId;

    @Column(name = "first_name", length = 150, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 150, nullable = false)
    private String lastName;

    @Column(name = "office_region", length = 100)
    private String officeRegion;

    @Column(name = "username", length = 100, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "officer_roles",
        joinColumns = @JoinColumn(name = "officer_id"))
    @Column(name = "role", length = 30)
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OfficerStatus status = OfficerStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
