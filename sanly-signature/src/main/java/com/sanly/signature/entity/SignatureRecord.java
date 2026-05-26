package com.sanly.signature.entity;

import com.sanly.signature.config.EncryptedStringConverter;
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
    name = "signature_records",
    indexes = {
        @Index(name = "idx_sig_national_id", columnList = "signer_national_id"),
        @Index(name = "idx_sig_status",      columnList = "status"),
        @Index(name = "idx_sig_signed_at",   columnList = "signed_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class SignatureRecord {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "signature_id", updatable = false, nullable = false)
    private UUID signatureId;

    @Column(name = "signature_code", unique = true, nullable = false, length = 22)
    private String signatureCode;

    @Column(name = "signer_national_id", nullable = false, length = 11)
    private String signerNationalId;

    @Column(name = "document_hash", nullable = false, length = 64)
    private String documentHash;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "document_name", columnDefinition = "TEXT")
    private String documentName;

    @Column(name = "document_size_bytes")
    private Long documentSizeBytes;

    @Column(name = "signature_value", nullable = false, length = 128)
    private String signatureValue;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private SignatureStatus status = SignatureStatus.VALID;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "revoked_reason", columnDefinition = "TEXT")
    private String revokedReason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
