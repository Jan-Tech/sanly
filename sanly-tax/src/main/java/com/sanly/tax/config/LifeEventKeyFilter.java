package com.sanly.tax.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates internal life-event automation calls from sanly-civil.
 * Validates the X-Life-Event-Key header against the shared pre-configured secret.
 * Only activates on /api/v1/benefits/** and /api/v1/taxpayers/update-marital-status.
 */
@Slf4j
@Component
public class LifeEventKeyFilter extends OncePerRequestFilter {

    @Value("${life-event.service-key:change-me-life-event-key}")
    private String configuredKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/api/v1/benefits")
            && !uri.equals("/api/v1/taxpayers/update-marital-status")
            && !uri.equals("/api/v1/taxpayers/deregister");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String key = request.getHeader("X-Life-Event-Key");
        if (!StringUtils.hasText(key) || !configuredKey.equals(key)) {
            log.warn("Life event key auth failed for URI={}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid life event service key");
            return;
        }
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "civil-registry", null, List.of(new SimpleGrantedAuthority("ROLE_LIFE_EVENT_SERVICE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(request, response);
    }
}
