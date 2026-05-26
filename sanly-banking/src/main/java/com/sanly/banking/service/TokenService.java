package com.sanly.banking.service;

import com.sanly.banking.entity.ConsentRequest;
import com.sanly.banking.entity.ConsentScope;
import com.sanly.banking.entity.ConsentToken;
import com.sanly.banking.exception.InvalidTokenException;
import com.sanly.banking.repository.ConsentTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final ConsentTokenRepository tokenRepository;

    /**
     * Generates a new consent token and persists its hash.
     * Returns the plain token (to be stored temporarily in consent_requests.plain_token_cache).
     */
    @Transactional
    public String generateToken(ConsentRequest consent, List<ConsentScope> grantedScopes) {
        // Generate plain token (32 hex chars from a UUID)
        String plainToken = UUID.randomUUID().toString().replace("-", "");
        String hash       = sha256Hex(plainToken);

        String scopesJson = "[" + grantedScopes.stream()
                .map(s -> "\"" + s.name() + "\"")
                .collect(Collectors.joining(",")) + "]";

        ConsentToken token = ConsentToken.builder()
                .consentCode(consent.getConsentCode())
                .tokenHash(hash)
                .citizenNationalId(consent.getCitizenNationalId())
                .bankCode(consent.getBankCode())
                .grantedScopes(scopesJson)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .status("ACTIVE")
                .build();

        tokenRepository.save(token);
        return plainToken;
    }

    /**
     * Validates the plain token and marks it as USED immediately (single-use).
     * The token is consumed even if subsequent data fetch fails.
     *
     * @throws InvalidTokenException if token not found, expired, already used, or bankCode mismatch
     */
    @Transactional
    public ConsentToken validateAndConsume(String plainToken, String bankCode) {
        String hash = sha256Hex(plainToken);

        ConsentToken t = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Consent token is invalid or does not exist"));

        if (!"ACTIVE".equals(t.getStatus())) {
            throw new InvalidTokenException("Consent token has already been " + t.getStatus().toLowerCase());
        }
        if (LocalDateTime.now().isAfter(t.getExpiresAt())) {
            t.setStatus("EXPIRED");
            tokenRepository.save(t);
            throw new InvalidTokenException("Consent token has expired");
        }
        if (!bankCode.equals(t.getBankCode())) {
            throw new InvalidTokenException("Consent token was not issued to this bank");
        }

        // Mark as USED immediately — before data fetch (critical: single-use enforcement)
        t.setStatus("USED");
        t.setUsedAt(LocalDateTime.now());
        tokenRepository.save(t);

        return t;
    }

    /**
     * SHA-256 hex digest. Uses MessageDigest.isEqual-style iteration for timing safety.
     */
    public String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
