package com.sanly.banking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_data_accesses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDataAccess {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "access_id", updatable = false, nullable = false)
    private UUID accessId;

    @Column(name = "bank_code", nullable = false, length = 20)
    private String bankCode;

    @Column(name = "citizen_national_id", nullable = false, length = 20)
    private String citizenNationalId;

    @Column(name = "consent_code", nullable = false, length = 30)
    private String consentCode;

    @Column(name = "scopes_accessed", columnDefinition = "TEXT")
    private String scopesAccessed; // JSON array string

    @Column(name = "accessed_at")
    private LocalDateTime accessedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "response_status", length = 20)
    private String responseStatus; // SUCCESS, PARTIAL, FAILED
}
