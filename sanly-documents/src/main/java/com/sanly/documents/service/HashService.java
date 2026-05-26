package com.sanly.documents.service;

import com.sanly.documents.entity.DocumentCertificate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class HashService {

    @Value("${signing.cert-secret}")
    private String certSecret;

    /**
     * Computes SHA-256 hash of the certificate's key fields plus a server secret.
     * This creates a tamper-evident seal on the certificate data.
     */
    public String computeHash(String certCode, String nationalId, String docType,
                               String sourceCode, String issuedAt) {
        String raw = certCode + "|" + nationalId + "|" + docType + "|"
                + sourceCode + "|" + issuedAt + "|" + certSecret;
        return sha256Hex(raw);
    }

    /**
     * Re-computes the hash for a given certificate and compares it against the stored hash.
     */
    public boolean verify(DocumentCertificate cert) {
        String expected = computeHash(
                cert.getCertificateCode(),
                cert.getCitizenNationalId(),
                cert.getDocumentType().name(),
                cert.getSourceRecordCode() != null ? cert.getSourceRecordCode() : "",
                cert.getIssuedAt().toString()
        );
        return expected.equals(cert.getVerificationHash());
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
