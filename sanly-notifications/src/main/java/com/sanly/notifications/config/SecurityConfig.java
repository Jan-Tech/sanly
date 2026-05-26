package com.sanly.notifications.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ServiceKeyAuthFilter    serviceKeyFilter;
    private final RateLimitingFilter      rateLimitingFilter;
    private final AuthEntryPoint          authEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(authEntryPoint))
            .authorizeHttpRequests(a -> a
                // Service-to-service send endpoints — authenticated by ServiceKeyAuthFilter (ROLE_SERVICE)
                .requestMatchers("/api/v1/notifications/send", "/api/v1/notifications/send-bulk")
                    .hasRole("SERVICE")
                // Admin endpoints — ROLE_ADMIN from citizen-registry JWT
                .requestMatchers("/api/v1/notifications/admin/**").hasRole("ADMIN")
                // Citizen endpoints — any authenticated user (ROLE_CITIZEN or ROLE_ADMIN)
                .requestMatchers("/api/v1/notifications/my/**").authenticated()
                // Swagger — public (used by docker health check)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated())
            // ServiceKeyFilter runs before JWT filter — handles /send paths exclusively
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(serviceKeyFilter,   UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter,          UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
