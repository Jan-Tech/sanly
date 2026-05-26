package com.sanly.registry.controller;

import com.sanly.registry.config.JwtTokenProvider;
import com.sanly.registry.config.UserDetailsImpl;
import com.sanly.registry.dto.request.AuthRequest;
import com.sanly.registry.dto.request.ResendOtpRequest;
import com.sanly.registry.dto.request.VerifyActionOtpRequest;
import com.sanly.registry.dto.request.VerifyOtpRequest;
import com.sanly.registry.dto.response.ActionOtpResponse;
import com.sanly.registry.dto.response.ApiResponse;
import com.sanly.registry.dto.response.LoginResponse;
import com.sanly.registry.dto.response.SessionResponse;
import com.sanly.registry.entity.ActiveSession;
import com.sanly.registry.entity.Citizen;
import com.sanly.registry.client.NotificationClient;
import com.sanly.registry.entity.OtpPurpose;
import com.sanly.registry.repository.CitizenRepository;
import com.sanly.registry.service.OtpService;
import com.sanly.registry.service.SessionService;
import com.sanly.registry.util.UserAgentParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Two-step JWT authentication with SMS OTP for citizens")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider      tokenProvider;
    private final CitizenRepository     citizenRepository;
    private final OtpService            otpService;
    private final SessionService        sessionService;
    private final NotificationClient    notificationClient;

    @Value("${trusted-service-key:}") private String trustedServiceKey;

    // ─────────────────────────────────────────────────────────────────────────
    // Step 1: Credentials
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Step 1 — submit credentials. Citizens receive OTP challenge; admins receive JWT directly.")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody AuthRequest request,
                                            HttpServletRequest httpRequest) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        Set<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Admin / institution / officer → direct JWT, no OTP
        if (!roles.contains("ROLE_CITIZEN")) {
            String token = tokenProvider.generateToken(principal);
            log.info("Direct login: user={} roles={}", principal.getUsername(), roles);
            return ApiResponse.<LoginResponse>builder()
                    .success(true).message("Login successful")
                    .data(LoginResponse.builder()
                            .status("SUCCESS")
                            .token(token).tokenType("Bearer")
                            .expiresIn(tokenProvider.getExpirationMs())
                            .username(principal.getUsername()).roles(roles)
                            .build())
                    .build();
        }

        // Citizen → OTP challenge
        String nationalId = principal.getNationalId();
        Citizen citizen = citizenRepository.findById(nationalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Citizen record not found for this account."));

        if (citizen.getPhoneNumber() == null || citizen.getPhoneNumber().isBlank()) {
            log.warn("Citizen NIN={} has no phone number registered", nationalId);
            return ApiResponse.<LoginResponse>builder()
                    .success(false).message("No phone number on file. Please visit a registry office.")
                    .data(LoginResponse.builder().status("PHONE_REQUIRED").build())
                    .build();
        }

        String ip = resolveClientIp(httpRequest);
        String sessionToken = otpService.createAndSend(
                nationalId, citizen.getPhoneNumber(),
                request.getLanguage() != null ? request.getLanguage() : "EN", ip);

        String phoneMasked = "***" + citizen.getPhoneNumber()
                .substring(Math.max(0, citizen.getPhoneNumber().length() - 4));
        log.info("OTP sent: NIN={} phone={}**** ip={}", nationalId, phoneMasked, ip);

        return ApiResponse.<LoginResponse>builder()
                .success(true).message("OTP sent to registered phone number.")
                .data(LoginResponse.builder()
                        .status("OTP_REQUIRED")
                        .sessionToken(sessionToken)
                        .phoneMasked(phoneMasked)
                        .attemptsRemaining(3)
                        .build())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 2: OTP verification
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/verify-otp")
    @Operation(summary = "Step 2 — verify the 6-digit OTP and receive a JWT token.")
    public ApiResponse<LoginResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request,
                                                HttpServletRequest httpRequest) {
        otpService.verify(request.getSessionToken(), request.getOtpCode());

        String nationalId = otpService.getNationalId(request.getSessionToken());

        String ip        = resolveClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        ActiveSession session = sessionService.createSession(nationalId, ip, userAgent);

        String token = tokenProvider.generateTokenForCitizen(nationalId, session.getSessionId().toString());

        log.info("OTP verified: NIN={} sessionId={}", nationalId, session.getSessionId());

        // Async: notify citizen of new sign-in
        String deviceName = UserAgentParser.parse(userAgent);
        notificationClient.send(nationalId, "NEW_SESSION_DETECTED", "EN",
                Map.of("deviceName", deviceName, "ipAddress", ip != null ? ip : "unknown"));

        return ApiResponse.<LoginResponse>builder()
                .success(true).message("Authentication successful.")
                .data(LoginResponse.builder()
                        .status("SUCCESS")
                        .token(token).tokenType("Bearer")
                        .expiresIn(tokenProvider.getExpirationMs())
                        .sessionId(session.getSessionId())
                        .build())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Resend OTP
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP to the same phone (max 3 resends per session).")
    public ApiResponse<Void> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        String nationalId = otpService.getNationalId(request.getSessionToken());
        Citizen citizen = citizenRepository.findById(nationalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Citizen not found."));

        otpService.resend(request.getSessionToken(), citizen.getPhoneNumber(),
                request.getLanguage() != null ? request.getLanguage() : "EN");

        return ApiResponse.<Void>builder().success(true).message("OTP resent.").build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Session management (citizen)
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/sessions")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "List the citizen's active sessions.")
    public ApiResponse<List<SessionResponse>> listSessions(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(required = false) UUID currentSessionId) {
        List<SessionResponse> sessions =
                sessionService.listActiveSessions(principal.getNationalId(), currentSessionId);
        return ApiResponse.<List<SessionResponse>>builder().success(true).data(sessions).build();
    }

    @GetMapping("/sessions/current")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Get details of the current session.")
    public ApiResponse<SessionResponse> getCurrentSession(@RequestParam UUID sessionId) {
        return ApiResponse.<SessionResponse>builder()
                .success(true)
                .data(sessionService.getCurrentSession(sessionId))
                .build();
    }

    @DeleteMapping("/sessions/{sessionId}")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Revoke a specific session by ID.")
    public ApiResponse<Void> revokeSession(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @PathVariable UUID sessionId) {
        sessionService.revokeSession(principal.getNationalId(), sessionId);
        return ApiResponse.<Void>builder().success(true).message("Session revoked.").build();
    }

    @DeleteMapping("/sessions/other")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Revoke all sessions except the current one.")
    public ApiResponse<Void> revokeOtherSessions(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam UUID currentSessionId) {
        int count = sessionService.revokeAllOther(principal.getNationalId(), currentSessionId);
        return ApiResponse.<Void>builder().success(true)
                .message(count + " other session(s) revoked.").build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin session management
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/admin/citizens/{nationalId}/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: view all active sessions for a citizen.")
    public ApiResponse<List<SessionResponse>> adminListSessions(@PathVariable String nationalId) {
        return ApiResponse.<List<SessionResponse>>builder()
                .success(true)
                .data(sessionService.listAllSessionsForAdmin(nationalId))
                .build();
    }

    @DeleteMapping("/admin/citizens/{nationalId}/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: revoke all sessions for a citizen (e.g. account compromise).")
    public ApiResponse<Void> adminRevokeSessions(@PathVariable String nationalId) {
        int count = sessionService.revokeAllForAdmin(nationalId);
        if (count > 0) {
            notificationClient.send(nationalId, "SESSION_REVOKED_BY_ADMIN", "EN", Map.of());
        }
        return ApiResponse.<Void>builder().success(true)
                .message("All " + count + " session(s) revoked for NIN=" + nationalId + ".").build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sensitive-action OTP (for signing, high-risk operations)
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/generate-otp-for-action")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Generate a SENSITIVE_ACTION OTP for the authenticated citizen. Required before signing a document.")
    public ApiResponse<ActionOtpResponse> generateActionOtp(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(defaultValue = "EN") String language,
            HttpServletRequest httpRequest) {

        String nationalId = principal.getNationalId();
        Citizen citizen = citizenRepository.findById(nationalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Citizen record not found."));

        if (citizen.getPhoneNumber() == null || citizen.getPhoneNumber().isBlank()) {
            return ApiResponse.<ActionOtpResponse>builder()
                    .success(false).message("No phone number registered.")
                    .data(ActionOtpResponse.builder().sent(false).build())
                    .build();
        }

        String ip = resolveClientIp(httpRequest);
        otpService.createAndSendForPurpose(nationalId, citizen.getPhoneNumber(), language, ip, OtpPurpose.SENSITIVE_ACTION);

        String phoneMasked = "***" + citizen.getPhoneNumber()
                .substring(Math.max(0, citizen.getPhoneNumber().length() - 4));

        log.info("SENSITIVE_ACTION OTP generated for NIN={}", nationalId);
        return ApiResponse.<ActionOtpResponse>builder()
                .success(true).message("OTP sent.")
                .data(ActionOtpResponse.builder().sent(true).phoneMasked(phoneMasked).build())
                .build();
    }

    @PostMapping("/verify-action-otp")
    @Operation(summary = "Service-to-service: verify a SENSITIVE_ACTION OTP. Requires X-Service-Key header.")
    public ApiResponse<ActionOtpResponse> verifyActionOtp(
            @RequestHeader("X-Service-Key") String serviceKey,
            @Valid @RequestBody VerifyActionOtpRequest request) {

        if (!trustedServiceKey.equals(serviceKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service key.");
        }

        boolean valid = otpService.verifyForPurpose(request.getNationalId(), request.getOtpCode(),
                OtpPurpose.SENSITIVE_ACTION);

        log.info("SENSITIVE_ACTION OTP verify: NIN={} valid={}", request.getNationalId(), valid);
        return ApiResponse.<ActionOtpResponse>builder()
                .success(true)
                .data(ActionOtpResponse.builder().valid(valid).build())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
