package com.sanly.police.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Arrays.copyOf(secret.getBytes(StandardCharsets.UTF_8), 32));
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetailsImpl principal) {
        Date now = new Date(), exp = new Date(now.getTime() + expirationMs);
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        return Jwts.builder().subject(principal.getUsername()).claim("roles", roles)
                .issuedAt(now).expiration(exp).signWith(key).compact();
    }

    public boolean validateToken(String token) {
        try { parseClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT invalid: {}", e.getMessage()); return false;
        }
    }

    public String getUsernameFromToken(String token) { return parseClaims(token).getSubject(); }
    public long getExpirationMs() { return expirationMs; }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
