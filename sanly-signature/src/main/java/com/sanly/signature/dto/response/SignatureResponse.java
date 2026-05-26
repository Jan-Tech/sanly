package com.sanly.signature.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sanly.signature.entity.SignatureRecord;
import com.sanly.signature.entity.SignatureStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignatureResponse {

    private UUID signatureId;
    private String signatureCode;
    private String signerNationalId;
    private String documentHash;
    private String documentName;
    private Long documentSizeBytes;
    private String signatureValue;
    private String purpose;
    private LocalDateTime signedAt;
    private SignatureStatus status;
    private LocalDateTime revokedAt;
    private String revokedReason;
    private String ipAddress;
    private LocalDateTime createdAt;

    /** Present only in detail view — logs are already loaded by the service. */
    private List<VerificationLogResponse> verificationLogs;

    /**
     * Maps a SignatureRecord to a SignatureResponse.
     * Because the entity uses @Convert(EncryptedStringConverter), JPA auto-decrypts
     * documentName, purpose, and revokedReason on load — we can map them directly.
     */
    public static SignatureResponse from(SignatureRecord record) {
        return SignatureResponse.builder()
                .signatureId(record.getSignatureId())
                .signatureCode(record.getSignatureCode())
                .signerNationalId(record.getSignerNationalId())
                .documentHash(record.getDocumentHash())
                .documentName(record.getDocumentName())
                .documentSizeBytes(record.getDocumentSizeBytes())
                .signatureValue(record.getSignatureValue())
                .purpose(record.getPurpose())
                .signedAt(record.getSignedAt())
                .status(record.getStatus())
                .revokedAt(record.getRevokedAt())
                .revokedReason(record.getRevokedReason())
                .ipAddress(record.getIpAddress())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
