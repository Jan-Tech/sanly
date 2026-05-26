package com.sanly.banking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consent_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentToken {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "token_id", updatable = false, nullable = false)
    private UUID tokenId;

    @Column(name = "consent_code", nullable = false, length = 30)
    private String consentCode;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash; // SHA-256 hex of plain token

    @Column(name = "citizen_national_id", nullable = false, length = 20)
    private String citizenNationalId;

    @Column(name = "bank_code", nullable = false, length = 20)
    private String bankCode;

    @Column(name = "granted_scopes", columnDefinition = "TEXT")
    private String grantedScopes; // JSON array string

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt; // nullable — single-use

    @Column(name = "status", length = 20)
    private String status;
}
