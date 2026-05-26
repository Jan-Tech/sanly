package com.sanly.banking.config;

import com.sanly.banking.entity.BankingOfficer;
import com.sanly.banking.repository.BankingOfficerRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Dual-mode JWT filter:
 * - If JWT has "nationalId" claim → set principal as String (citizen's NIN) with ROLE_CITIZEN
 * - If JWT has "sub" only → load officer from DB by username
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider        tokenProvider;
    private final OfficerUserDetailsService userDetailsService;
    private final BankingOfficerRepository officerRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {

        String token = extractBearerToken(request);
        if (token != null && tokenProvider.validateToken(token)) {
            try {
                Claims claims = tokenProvider.getClaims(token);
                String nationalId = (String) claims.get("nationalId");

                if (nationalId != null) {
                    // Citizen token — set principal as nationalId string
                    var auth = new UsernamePasswordAuthenticationToken(
                            nationalId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_CITIZEN"))
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    // Officer token — load from DB by username
                    String username = claims.getSubject();
                    OfficerUserDetails ud = (OfficerUserDetails) userDetailsService.loadUserByUsername(username);
                    var auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                log.warn("Could not set user authentication from token: {}", e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
