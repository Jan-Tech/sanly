package com.sanly.documents.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sanly.documents.entity.CertificateStatus;
import com.sanly.documents.entity.DocumentType;
import com.sanly.documents.entity.SourceService;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CertificateResponse {

    private UUID certificateId;
    private String certificateCode;
    private String citizenNationalId;
    private String holderName;
    private DocumentType documentType;
    private SourceService sourceService;
    private String sourceRecordCode;
    private String title;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private CertificateStatus status;
    private String verificationHash;
    private int downloadCount;
    private LocalDateTime lastDownloadedAt;
    private LocalDateTime revokedAt;
    private String revokeReason;
    private long verificationCount;
}
