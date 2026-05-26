package com.sanly.banking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "registered_banks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredBank {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "bank_id", updatable = false, nullable = false)
    private UUID bankId;

    @Column(name = "bank_code", unique = true, nullable = false, length = 20)
    private String bankCode;

    @Column(name = "bank_name", nullable = false, length = 200)
    private String bankName;

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "contact_email", length = 200)
    private String contactEmail;

    @Column(name = "api_key_hash", length = 255)
    private String apiKeyHash;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by_officer_id")
    private UUID approvedByOfficerId;
}
