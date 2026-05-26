package com.sanly.registry.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

/**
 * Unified login response covering both direct JWT (admin/institution) and OTP-required (citizen) flows.
 *
 * status values:
 *   "OTP_REQUIRED"   — OTP sent to phoneMasked; client must call /auth/verify-otp
 *   "PHONE_REQUIRED" — Citizen account has no phone; must register in person
 *   "SUCCESS"        — JWT issued directly (admin/institution bypass 2FA)
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private String status;

    // ── OTP_REQUIRED fields ─────────────────────────────────────────────
    private String sessionToken;
    private String phoneMasked;
    private Integer attemptsRemaining;

    // ── SUCCESS fields ───────────────────────────────────────────────────
    private String token;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private Set<String> roles;
    private UUID sessionId;

    // ── Error message ───────────────────────────────────────────────────
    private String message;
}
