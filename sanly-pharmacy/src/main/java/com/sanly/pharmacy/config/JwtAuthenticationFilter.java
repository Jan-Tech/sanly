package com.sanly.pharmacy.config;

import com.sanly.pharmacy.entity.PharmacyStaff;
import com.sanly.pharmacy.entity.StaffStatus;
import com.sanly.pharmacy.repository.PharmacyStaffRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider       jwtTokenProvider;
    private final PharmacyStaffRepository staffRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenProvider.validate(token)) {
                String username = jwtTokenProvider.getUsername(token);
                staffRepository.findByUsernameAndStatus(username, StaffStatus.ACTIVE)
                        .ifPresent(staff -> {
                            UserDetailsImpl details = new UserDetailsImpl(staff);
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            }
        }
        chain.doFilter(request, response);
    }
}
