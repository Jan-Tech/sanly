package com.sanly.medical.config;

import com.sanly.medical.entity.PharmacyStatus;
import com.sanly.medical.repository.RegisteredPharmacyRepository;
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
 * Authenticates dispensing requests from registered pharmacies.
 * Intercepts POST /api/v1/prescriptions/{code}/dispense.
 * Verifies X-Pharmacy-Code + X-Pharmacy-Key headers against the pharmacy's BCrypt key hash.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PharmacyKeyAuthFilter extends OncePerRequestFilter {

    private final RegisteredPharmacyRepository pharmacyRepository;
    private final PasswordEncoder              passwordEncoder;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        String uri    = request.getRequestURI();
        // Only intercepts POST .../dispense — all other paths handled by JWT filter
        return !("POST".equals(method) && uri.matches("/api/v1/prescriptions/.+/dispense"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String pharmacyCode = request.getHeader("X-Pharmacy-Code");
        String pharmacyKey  = request.getHeader("X-Pharmacy-Key");

        if (!StringUtils.hasText(pharmacyCode) || !StringUtils.hasText(pharmacyKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "X-Pharmacy-Code and X-Pharmacy-Key headers required");
            return;
        }

        boolean authenticated = pharmacyRepository
                .findByPharmacyCodeAndStatus(pharmacyCode, PharmacyStatus.ACTIVE)
                .map(pharmacy -> passwordEncoder.matches(pharmacyKey, pharmacy.getApiKeyHash()))
                .orElse(false);

        if (!authenticated) {
            log.warn("Pharmacy key authentication failed for code: {}", pharmacyCode);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or suspended pharmacy key");
            return;
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                pharmacyCode, null, List.of(new SimpleGrantedAuthority("ROLE_PHARMACY")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }
}
