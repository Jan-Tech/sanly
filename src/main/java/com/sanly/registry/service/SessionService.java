package com.sanly.registry.service;

import com.sanly.registry.dto.response.SessionResponse;
import com.sanly.registry.entity.ActiveSession;
import com.sanly.registry.entity.SessionStatus;
import com.sanly.registry.repository.ActiveSessionRepository;
import com.sanly.registry.util.UserAgentParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final ActiveSessionRepository sessionRepo;

    @Value("${session.expiry-hours:24}") private int expiryHours;

    @Transactional
    public ActiveSession createSession(String nationalId, String ipAddress, String userAgent) {
        String fingerprint = fingerprint(userAgent, ipAddress);
        return sessionRepo.save(ActiveSession.builder()
                .nationalId(nationalId)
                .deviceFingerprint(fingerprint)
                .createdAt(LocalDateTime.now())
                .lastSeenAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(expiryHours))
                .status(SessionStatus.ACTIVE)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceName(UserAgentParser.parse(userAgent))
                .lastAction("Authentication")
                .build());
    }

    /** Called by SessionTrackingFilter on every authenticated request. */
    @Transactional
    public void touchSession(UUID sessionId, String uri) {
        sessionRepo.findById(sessionId).ifPresent(s -> {
            s.setLastSeenAt(LocalDateTime.now());
            s.setLastAction(UserAgentParser.categorizeUri(uri));
            sessionRepo.save(s);
        });
    }

    @Transactional(readOnly = true)
    public SessionResponse getCurrentSession(UUID sessionId) {
        ActiveSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Session not found"));
        return toResponse(s, sessionId);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listActiveSessions(String nationalId, UUID currentSessionId) {
        return sessionRepo.findByNationalIdAndStatusOrderByCreatedAtDesc(nationalId, SessionStatus.ACTIVE)
                .stream().map(s -> toResponse(s, currentSessionId)).toList();
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listAllSessionsForAdmin(String nationalId) {
        return sessionRepo.findByNationalIdAndStatusOrderByCreatedAtDesc(nationalId, SessionStatus.ACTIVE)
                .stream().map(s -> toResponse(s, null)).toList();
    }

    @Transactional
    public void revokeSession(String nationalId, UUID sessionId) {
        ActiveSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        if (!session.getNationalId().equals(nationalId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your session");
        session.setStatus(SessionStatus.REVOKED);
        sessionRepo.save(session);
    }

    @Transactional
    public int revokeAllOther(String nationalId, UUID currentSessionId) {
        return sessionRepo.revokeAllExcept(nationalId, currentSessionId);
    }

    @Transactional
    public int revokeAllForAdmin(String nationalId) {
        return sessionRepo.revokeAllForNationalId(nationalId);
    }

    private static String fingerprint(String userAgent, String ipAddress) {
        try {
            String raw = (userAgent != null ? userAgent : "") + "|" + (ipAddress != null ? ipAddress : "");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { return "unknown"; }
    }

    private SessionResponse toResponse(ActiveSession s, UUID currentSessionId) {
        return SessionResponse.builder()
                .sessionId(s.getSessionId()).nationalId(s.getNationalId())
                .createdAt(s.getCreatedAt()).lastSeenAt(s.getLastSeenAt())
                .expiresAt(s.getExpiresAt()).status(s.getStatus().name())
                .ipAddress(s.getIpAddress()).userAgent(s.getUserAgent())
                .deviceName(s.getDeviceName()).lastAction(s.getLastAction())
                .currentSession(currentSessionId != null && s.getSessionId().equals(currentSessionId))
                .build();
    }
}
