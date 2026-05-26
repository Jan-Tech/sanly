package com.sanly.documents.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_certificates",
        indexes = {
            @Index(name = "idx_cert_national_id", columnList = "citizen_national_id"),
            @Index(name = "idx_cert_status",      columnList = "status"),
            @Index(name = "idx_cert_type",        columnList = "document_type")
        })
@Getter
@Setter
public class DocumentCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "certificate_id", updatable = false, nullable = false)
    private UUID certificateId;

    @Column(name = "certificate_code", length = 22, unique = true, nullable = false)
    private String certificateCode;

    @Column(name = "citizen_national_id", length = 11, nullable = false)
    private String citizenNationalId;

    @Column(name = "holder_name", length = 200, nullable = false)
    private String holderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 30, nullable = false)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_service", length = 20, nullable = false)
    private SourceService sourceService;

    @Column(name = "source_record_code", length = 50)
    private String sourceRecordCode;

    @Column(name = "title", length = 300)
    private String title;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private CertificateStatus status = CertificateStatus.VALID;

    @Column(name = "verification_hash", length = 64, nullable = false)
    private String verificationHash;

    @Column(name = "download_count", nullable = false)
    private int downloadCount = 0;

    @Column(name = "last_downloaded_at")
    private LocalDateTime lastDownloadedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoke_reason", length = 500)
    private String revokeReason;

    @Column(name = "pdf_content", columnDefinition = "TEXT")
    private String pdfContent;
}
