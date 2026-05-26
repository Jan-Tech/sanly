package com.sanly.court.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final RateLimitingFilter      rateLimitingFilter;
    private final AuthEntryPoint          authEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter j, RateLimitingFilter r, AuthEntryPoint a) {
        this.jwtFilter = j; this.rateLimitingFilter = r; this.authEntryPoint = a;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(authEntryPoint))
            .authorizeHttpRequests(a -> a
                // Public
                .requestMatchers("/api/v1/court/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/court/cases/verify/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/court/documents/verify/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/court/fines/verify/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Court management — ADMIN only
                .requestMatchers(HttpMethod.POST,  "/api/v1/court/courts").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/court/courts/**").hasRole("ADMIN")
                // Judge assignment — ADMIN
                .requestMatchers(HttpMethod.PATCH, "/api/v1/court/cases/*/assign-judge").hasRole("ADMIN")
                // Verdicts — JUDGE only
                .requestMatchers(HttpMethod.POST, "/api/v1/court/cases/*/verdict").hasRole("JUDGE")
                // Judge waiving fines — JUDGE only
                .requestMatchers(HttpMethod.PATCH, "/api/v1/court/fines/*/waive").hasRole("JUDGE")
                // Officer management — ADMIN only
                .requestMatchers("/api/v1/court/officers/**").hasRole("ADMIN")
                // Citizen-pay — any authenticated user (portal uses admin token)
                .requestMatchers(HttpMethod.PATCH, "/api/v1/court/fines/*/citizen-pay").authenticated()
                // Everything else — authenticated
                .anyRequest().authenticated())
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter,          UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }
}
