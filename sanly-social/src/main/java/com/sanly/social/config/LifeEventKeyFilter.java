package com.sanly.social.config;

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

@Slf4j
@Component
public class LifeEventKeyFilter extends OncePerRequestFilter {

    @Value("${life-event.service-key:change-me-life-event-key}")
    private String configuredKey;

    private static final List<String> PROTECTED = List.of(
            "/api/v1/social/claims/auto-trigger",
            "/api/v1/social/claims/cancel-all"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return PROTECTED.stream().noneMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String key = req.getHeader("X-Life-Event-Key");
        if (!StringUtils.hasText(key) || !configuredKey.equals(key)) {
            log.warn("Life event key auth failed for {}", req.getRequestURI());
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid life event service key");
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("civil-registry", null,
                        List.of(new SimpleGrantedAuthority("ROLE_LIFE_EVENT_SERVICE"))));
        chain.doFilter(req, res);
    }
}
