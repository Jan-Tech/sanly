package com.sanly.documents.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Dual-mode JWT filter:
 * - Citizen JWT (has "nationalId" claim) -> principal = nationalId String, authority ROLE_CITIZEN
 * - Admin JWT (no "nationalId" claim) -> load from DB via UserDetailsServiceImpl
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String nationalId = jwtTokenProvider.getNationalIdFromToken(token);
                    if (nationalId != null && !nationalId.isBlank()) {
                        // Citizen JWT — principal is the national ID string
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_CITIZEN"));
                        var auth = new UsernamePasswordAuthenticationToken(nationalId, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        // Admin JWT — load from DB
                        String username = jwtTokenProvider.getUsernameFromToken(token);
                        var userDetails = userDetailsService.loadUserByUsername(username);
                        var auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception ignored) {
                // Invalid token — proceed unauthenticated
            }
        }
        chain.doFilter(request, response);
    }
}
