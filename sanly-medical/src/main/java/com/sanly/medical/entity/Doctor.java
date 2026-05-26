package com.sanly.medical.entity;

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
 * Represents both regular doctors (ROLE_DOCTOR, linked to a clinic) and
 * admin accounts (ROLE_ADMIN, clinicId = null, nationalId = null).
 * Using a single table keeps auth loading simple.
 */
@Entity
@Table(name = "doctors",
    indexes = {
        @Index(name = "idx_doctors_clinic_id", columnList = "clinic_id"),
        @Index(name = "idx_doctors_status",    columnList = "status")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId;

    /** The doctor's own national ID (TM-NIN). Null for admin-only accounts. */
    @Column(name = "national_id", length = 11, unique = true)
    private String nationalId;

    @Column(name = "first_name", length = 150, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 150, nullable = false)
    private String lastName;

    @Column(name = "specialization", length = 200)
    private String specialization;

    @Column(name = "license_number", length = 100, unique = true)
    private String licenseNumber;

    /** Null for admin accounts. */
    @Column(name = "clinic_id")
    private Long clinicId;

    @Column(name = "username", length = 100, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "doctor_roles",
        joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "role", length = 30)
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DoctorStatus status = DoctorStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
