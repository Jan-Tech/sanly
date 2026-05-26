package com.sanly.registry.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Issues and validates HMAC-SHA256 JWTs.
 *
 * Token claims:
 *   sub        — username
 *   roles      — list of role strings (e.g. "ROLE_ADMIN")
 *   nationalId — citizen's NIN, present only for ROLE_CITIZEN accounts
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        // Ensure key is at least 32 bytes (256 bits) for HMAC-SHA256
        byte[] keyBytes = Arrays.copyOf(
                secret.getBytes(StandardCharsets.UTF_8), 32);
        this.signingKey   = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(Authentication authentication) {
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        return buildToken(principal);
    }

    public String generateToken(UserDetailsImpl principal) {
        return buildToken(principal);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    /**
     * Generates a JWT for a citizen after OTP verification. Includes sessionId so
     * SessionTrackingFilter can validate the session on every request.
     */
    public String generateTokenForCitizen(String nationalId, String sessionId) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(nationalId)
                .claim("roles", List.of("ROLE_CITIZEN"))
                .claim("nationalId", nationalId)
                .claim("sessionId", sessionId)
                .issuedAt(now).expiration(expiry).signWith(signingKey).compact();
    }

    public String getSessionIdFromToken(String token) {
        try {
            return parseClaims(token).get("sessionId", String.class);
        } catch (Exception e) { return null; }
    }

    // ---- private ----

    private String buildToken(UserDetailsImpl principal) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var builder = Jwts.builder()
                .subject(principal.getUsername())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey);

        if (principal.getNationalId() != null) {
            builder.claim("nationalId", principal.getNationalId());
        }

        return builder.compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
