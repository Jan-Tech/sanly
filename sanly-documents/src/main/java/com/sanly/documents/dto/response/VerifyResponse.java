package com.sanly.documents.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sanly.documents.entity.CertificateStatus;
import com.sanly.documents.entity.DocumentType;
import com.sanly.documents.entity.SourceService;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyResponse {

    private boolean valid;
    private String certificateCode;
    private DocumentType documentType;
    private String holderName;
    private String holderNationalId;   // masked, e.g. ***1234***
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private CertificateStatus status;
    private SourceService sourceService;
    private String sourceRecordCode;
    private String message;
}
