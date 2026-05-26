package com.sanly.signature.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "signature_verification_logs",
    indexes = {
        @Index(name = "idx_vlog_sig_code", columnList = "signature_code"),
        @Index(name = "idx_vlog_verifier", columnList = "verifier_national_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class SignatureVerificationLog {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "log_id", updatable = false, nullable = false)
    private UUID logId;

    @Column(name = "signature_code", nullable = false, length = 22)
    private String signatureCode;

    @CreationTimestamp
    @Column(name = "verified_at", nullable = false, updatable = false)
    private LocalDateTime verifiedAt;

    @Column(name = "verifier_ip", length = 45)
    private String verifierIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 15)
    private VerificationResult result;

    @Column(name = "verifier_national_id", length = 11)
    private String verifierNationalId;

    @Column(name = "document_resubmitted", nullable = false)
    private boolean documentResubmitted = false;
}
