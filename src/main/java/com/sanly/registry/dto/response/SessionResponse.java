package com.sanly.registry.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SessionResponse {
    private UUID sessionId;
    private String nationalId;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;
    private LocalDateTime expiresAt;
    private String status;
    private String ipAddress;
    private String userAgent;
    private String deviceName;
    private String lastAction;
    private boolean currentSession;
}
