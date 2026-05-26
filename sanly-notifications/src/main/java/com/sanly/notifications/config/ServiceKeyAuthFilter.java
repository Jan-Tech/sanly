package com.sanly.notifications.config;

import com.sanly.notifications.repository.ServiceAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates service-to-service calls on /api/v1/notifications/send* paths.
 * Callers set X-Service-Name and X-Service-Key headers.
 * The key is BCrypt-verified against the stored hash.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceKeyAuthFilter extends OncePerRequestFilter {

    private final ServiceAccountRepository serviceAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/notifications/send");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String serviceName = request.getHeader("X-Service-Name");
        String serviceKey  = request.getHeader("X-Service-Key");

        if (!StringUtils.hasText(serviceName) || !StringUtils.hasText(serviceKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "X-Service-Name and X-Service-Key headers required");
            return;
        }

        boolean authenticated = serviceAccountRepository
                .findByServiceNameAndActiveTrue(serviceName)
                .map(account -> passwordEncoder.matches(serviceKey, account.getKeyHash()))
                .orElse(false);

        if (!authenticated) {
            log.warn("Service key authentication failed for service: {}", serviceName);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid service key");
            return;
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                serviceName, null, List.of(new SimpleGrantedAuthority("ROLE_SERVICE")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }
}
