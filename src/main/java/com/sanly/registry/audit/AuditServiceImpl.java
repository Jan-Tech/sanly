package com.sanly.registry.audit;

import com.sanly.registry.config.UserDetailsImpl;
import com.sanly.registry.entity.AuditLog;
import com.sanly.registry.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async("auditExecutor")
    public void log(String action, String citizenNationalId, HttpServletRequest request) {
        persist(action, citizenNationalId, request, 200, null);
    }

    @Override
    @Async("auditExecutor")
    public void logFailure(String action, String citizenNationalId,
                           HttpServletRequest request, int statusCode, String details) {
        persist(action, citizenNationalId, request, statusCode, details);
    }

    private void persist(String action, String nationalId,
                         HttpServletRequest request, int statusCode, String details) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String performer = (auth != null) ? auth.getName() : "anonymous";
            String role      = extractPrimaryRole(auth);

            AuditLog entry = AuditLog.builder()
                    .action(action)
                    .citizenNationalId(nationalId)
                    .performedBy(performer)
                    .role(role)
                    .ipAddress(clientIp(request))
                    .endpoint(request.getRequestURI())
                    .httpMethod(request.getMethod())
                    .statusCode(statusCode)
                    .details(details)
                    .accessedAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(entry);
        } catch (Exception e) {
            // Audit must never break the main request
            log.error("Failed to persist audit log for action '{}': {}", action, e.getMessage());
        }
    }

    private String extractPrimaryRole(Authentication auth) {
        if (auth == null) return "ANONYMOUS";
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private String clientIp(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(h -> h.split(",")[0].trim())
                .orElse(request.getRemoteAddr());
    }
}
