package com.sanly.medical.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "clinics",
    indexes = {
        @Index(name = "idx_clinics_status", columnList = "status"),
        @Index(name = "idx_clinics_region", columnList = "region")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clinicId;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "license_number", length = 100, nullable = false, unique = true)
    private String licenseNumber;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "phone", length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ClinicStatus status = ClinicStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
