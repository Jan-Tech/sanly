package com.sanly.registry.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "active_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActiveSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id", updatable = false, nullable = false)
    private UUID sessionId;

    @Column(name = "national_id", nullable = false, length = 11)
    private String nationalId;

    @Column(name = "device_fingerprint", length = 64)
    private String deviceFingerprint;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /** Human-readable device name parsed from User-Agent, e.g. "Chrome on Windows". */
    @Column(name = "device_name", length = 100)
    private String deviceName;

    /** Last endpoint category accessed on this session, e.g. "Identity". */
    @Column(name = "last_action", length = 100)
    private String lastAction;
}
