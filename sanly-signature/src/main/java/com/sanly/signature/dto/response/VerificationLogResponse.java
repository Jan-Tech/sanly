package com.sanly.signature.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sanly.signature.entity.SignatureVerificationLog;
import com.sanly.signature.entity.VerificationResult;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationLogResponse {

    private UUID logId;
    private String signatureCode;
    private LocalDateTime verifiedAt;
    private String verifierIp;      // masked to first octet only, e.g. "213.*.*.*"
    private VerificationResult result;
    private boolean documentResubmitted;

    public static VerificationLogResponse from(SignatureVerificationLog log) {
        return VerificationLogResponse.builder()
                .logId(log.getLogId())
                .signatureCode(log.getSignatureCode())
                .verifiedAt(log.getVerifiedAt())
                .verifierIp(maskIp(log.getVerifierIp()))
                .result(log.getResult())
                .documentResubmitted(log.isDocumentResubmitted())
                .build();
    }

    private static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) return null;
        int firstDot = ip.indexOf('.');
        if (firstDot < 0) return ip; // IPv6 or unexpected format — return as-is
        return ip.substring(0, firstDot) + ".*.*.*";
    }
}
