package com.sanly.business.entity;

import com.sanly.business.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "businesses",
    indexes = {
        @Index(name = "idx_biz_owner",  columnList = "owner_national_id"),
        @Index(name = "idx_biz_status", columnList = "status"),
        @Index(name = "idx_biz_regnum", columnList = "registration_number")
    })
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Business {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID businessId;

    @Column(name = "registration_number", length = 25, nullable = false, unique = true)
    private String registrationNumber;

    @Column(name = "business_name", length = 300, nullable = false)
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", length = 25, nullable = false)
    private BusinessType businessType;

    @Column(name = "owner_national_id", length = 30, nullable = false)
    private String ownerNationalId;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "phone", length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    @Builder.Default
    private BusinessStatus status = BusinessStatus.ACTIVE;

    @Column(name = "approved_by_officer_id")
    private Long approvedByOfficerId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
