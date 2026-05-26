package com.sanly.court.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

/**
 * Computes and verifies SHA-256 document signatures.
 * Input: SHA-256(content + submitterNationalId + submittedAt.toString())
 *
 * Production note: This uses SHA-256 as a tamper-evident hash.
 * A production deployment would use RSA/ECDSA with national ID card chip signing
 * to provide non-repudiation guarantees.
 */
@Service
public class DocumentSigningService {

    public String computeSignature(String content, String submitterNationalId, LocalDateTime submittedAt) {
        try {
            String input = content + submitterNationalId + submittedAt.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Signature computation failed", e);
        }
    }

    public boolean verifySignature(String content, String submitterNationalId,
                                    LocalDateTime submittedAt, String storedSignature) {
        if (storedSignature == null) return false;
        return storedSignature.equals(computeSignature(content, submitterNationalId, submittedAt));
    }
}
