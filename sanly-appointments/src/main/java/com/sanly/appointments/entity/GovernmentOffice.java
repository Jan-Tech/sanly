package com.sanly.appointments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "government_offices",
    indexes = {
        @Index(name = "idx_offices_type", columnList = "institution_type"),
        @Index(name = "idx_offices_region", columnList = "region"),
        @Index(name = "idx_offices_status", columnList = "status")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernmentOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "office_id")
    private UUID officeId;

    @Column(name = "office_code", length = 12, unique = true, nullable = false)
    private String officeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "institution_type", length = 25, nullable = false)
    private InstitutionType institutionType;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "region", length = 100, nullable = false)
    private String region;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 25, nullable = false)
    @Builder.Default
    private OfficeStatus status = OfficeStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
