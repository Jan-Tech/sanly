package com.sanly.bridge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Generates and verifies institution API keys.
 *
 * Keys are random 32-byte hex strings prefixed with {@code sk_bridge_} for easy
 * identification (total ~73 chars). The raw key is NEVER stored — only its BCrypt
 * hash is persisted. The hash is computed with 12 rounds.
 *
 * Verification uses BCrypt's constant-time comparison to prevent timing attacks.
 */
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final SecureRandom RNG = new SecureRandom();
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private final PasswordEncoder passwordEncoder;

    /** Generate a new raw API key (returned once to the admin — never stored). */
    public String generateRawKey() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        StringBuilder sb = new StringBuilder("sk_bridge_");
        for (byte b : bytes) {
            sb.append(HEX[(b >> 4) & 0xF]);
            sb.append(HEX[b & 0xF]);
        }
        return sb.toString();
    }

    /** Hash a raw key for storage. */
    public String hash(String rawKey) {
        return passwordEncoder.encode(rawKey);
    }

    /** Constant-time verification of a raw key against its stored BCrypt hash. */
    public boolean verify(String rawKey, String storedHash) {
        return passwordEncoder.matches(rawKey, storedHash);
    }
}
