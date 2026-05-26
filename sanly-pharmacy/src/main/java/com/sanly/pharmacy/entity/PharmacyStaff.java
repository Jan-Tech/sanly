package com.sanly.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pharmacy_staff",
    indexes = {
        @Index(name = "idx_staff_username", columnList = "username", unique = true),
        @Index(name = "idx_staff_nin",      columnList = "national_id", unique = true)
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    /** TM-NIN of the pharmacist */
    @Column(name = "national_id", nullable = false, unique = true, length = 11)
    private String nationalId;

    @Column(name = "first_name", nullable = false, length = 150)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    /** Read from config — all staff in this instance belong to one pharmacy */
    @Column(name = "pharmacy_code", nullable = false, length = 20)
    private String pharmacyCode;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "role", nullable = false, length = 15)
    private StaffRole role = StaffRole.PHARMACIST;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 15)
    private StaffStatus status = StaffStatus.ACTIVE;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
