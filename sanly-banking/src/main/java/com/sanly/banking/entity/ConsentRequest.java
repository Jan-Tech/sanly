package com.sanly.banking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consent_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "consent_id", updatable = false, nullable = false)
    private UUID consentId;

    @Column(name = "consent_code", unique = true, nullable = false, length = 30)
    private String consentCode;

    @Column(name = "bank_code", nullable = false, length = 20)
    private String bankCode;

    @Column(name = "citizen_national_id", nullable = false, length = 20)
    private String citizenNationalId;

    @Column(name = "requested_scopes", columnDefinition = "TEXT")
    private String requestedScopes; // JSON array string

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose; // AES-256-GCM encrypted

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "otp_verified")
    private boolean otpVerified;

    @Column(name = "plain_token_cache", length = 64)
    private String plainTokenCache; // stored temporarily until bank retrieves it, then cleared
}
