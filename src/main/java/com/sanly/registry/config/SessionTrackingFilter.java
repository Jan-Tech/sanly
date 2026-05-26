package com.sanly.registry.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanly.registry.entity.ActiveSession;
import com.sanly.registry.entity.SessionStatus;
import com.sanly.registry.repository.ActiveSessionRepository;
import com.sanly.registry.util.UserAgentParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Validates that the session referenced by the JWT's "sessionId" claim is still ACTIVE.
 * Runs after JwtAuthenticationFilter so the token is already parsed.
 *
 * If a session has been revoked (by user or admin), this filter rejects the request
 * with 401 — even if the JWT itself hasn't expired. This makes session revocation
 * take effect immediately rather than waiting for JWT expiry.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionTrackingFilter extends OncePerRequestFilter {

    private final JwtTokenProvider        tokenProvider;
    private final ActiveSessionRepository sessionRepo;
    private final ObjectMapper            objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = extractBearerToken(request);
        if (token == null || !tokenProvider.validateToken(token)) {
            chain.doFilter(request, response);
            return;
        }

        String sessionIdStr = tokenProvider.getSessionIdFromToken(token);
        if (sessionIdStr == null) {
            // Admin or service token — no session binding, let through
            chain.doFilter(request, response);
            return;
        }

        UUID sessionId;
        try {
            sessionId = UUID.fromString(sessionIdStr);
        } catch (IllegalArgumentException e) {
            rejectRequest(response, "Invalid session token.");
            return;
        }

        ActiveSession session = sessionRepo.findById(sessionId).orElse(null);
        if (session == null || session.getStatus() == SessionStatus.REVOKED) {
            log.warn("Rejected request for revoked/missing sessionId={}", sessionId);
            rejectRequest(response, "Session has been revoked. Please sign in again.");
            return;
        }
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            rejectRequest(response, "Session has expired. Please sign in again.");
            return;
        }

        // Update last-seen and action category (only if more than 60s since last update)
        if (session.getLastSeenAt().isBefore(LocalDateTime.now().minusSeconds(60))) {
            session.setLastSeenAt(LocalDateTime.now());
            session.setLastAction(UserAgentParser.categorizeUri(request.getRequestURI()));
            sessionRepo.save(session);
        }

        chain.doFilter(request, response);
    }

    private void rejectRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("success", false, "message", message)));
    }

    private static String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
