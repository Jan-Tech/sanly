package com.sanly.signature.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sanly.signature.entity.SignatureStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyResponse {

    private boolean valid;
    private String signatureCode;

    /** Fetched from registry if bearerToken is available; null otherwise. */
    private String signerName;

    /** Masked: first 3 chars + "****" + last 3 chars, e.g. "123****456" */
    private String signerNationalId;

    private LocalDateTime signedAt;
    private String purpose;
    private String documentHash;
    private SignatureStatus status;

    /**
     * True when the caller re-uploaded the document file for hash comparison.
     * False when verifying by signature code only (metadata check).
     */
    private boolean documentResubmitted;

    private String message;
}
