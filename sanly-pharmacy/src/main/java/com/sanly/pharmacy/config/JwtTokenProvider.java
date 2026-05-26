package com.sanly.pharmacy.config;

import com.sanly.pharmacy.entity.PharmacyStaff;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long      expirationMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                             @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        this.key          = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generate(PharmacyStaff staff) {
        return Jwts.builder()
                .subject(staff.getUsername())
                .claims(Map.of(
                        "staffId",      staff.getStaffId(),
                        "role",         staff.getRole().name(),
                        "pharmacyCode", staff.getPharmacyCode()
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public boolean validate(String token) {
        try { parse(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    public String getUsername(String token)      { return parse(token).getSubject(); }
    public String getRole(String token)          { return (String) parse(token).get("role"); }
    public String getPharmacyCode(String token)  { return (String) parse(token).get("pharmacyCode"); }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
}
