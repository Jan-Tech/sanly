package com.sanly.documents.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificate_verification_logs",
        indexes = {
            @Index(name = "idx_vlog_cert_code", columnList = "certificate_code")
        })
@Getter
@Setter
public class CertificateVerificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id", updatable = false, nullable = false)
    private UUID logId;

    @Column(name = "certificate_code", length = 22, nullable = false)
    private String certificateCode;

    @CreationTimestamp
    @Column(name = "verified_at", nullable = false, updatable = false)
    private LocalDateTime verifiedAt;

    @Column(name = "verifier_ip", length = 45)
    private String verifierIp;

    @Column(name = "verifier_national_id", length = 11)
    private String verifierNationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 10, nullable = false)
    private VerificationResult result;
}
