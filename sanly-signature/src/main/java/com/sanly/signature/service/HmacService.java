package com.sanly.signature.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Component
public class HmacService {

    private final String secret;

    public HmacService(@Value("${signing.secret}") String secret) {
        this.secret = secret;
    }

    /**
     * Computes HMAC-SHA256 of the message:
     *   documentHash + "|" + nationalId + "|" + signedAt.toString()
     *
     * @return lowercase hex string of the HMAC
     */
    public String computeSignature(String documentHash, String nationalId, LocalDateTime signedAt) {
        try {
            String message = documentHash + "|" + nationalId + "|" + signedAt.toString();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC computation failed", e);
        }
    }

    /**
     * Verifies that the recomputed HMAC matches the expected signature.
     * Uses constant-time comparison to prevent timing attacks.
     */
    public boolean verify(String documentHash, String nationalId,
                          LocalDateTime signedAt, String expectedSignature) {
        if (expectedSignature == null) return false;
        String computed = computeSignature(documentHash, nationalId, signedAt);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
