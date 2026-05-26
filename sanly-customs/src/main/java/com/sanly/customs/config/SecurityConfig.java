package com.sanly.customs.config;

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

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          RateLimitingFilter rateLimitingFilter,
                          AuthEntryPoint authEntryPoint) {
        this.jwtFilter = jwtFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(authEntryPoint))
            .authorizeHttpRequests(a -> a
                // Public endpoints
                .requestMatchers("/api/v1/customs/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/customs/declarations/verify/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Port management — ADMIN only
                .requestMatchers(HttpMethod.POST,  "/api/v1/customs/ports").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/customs/ports/**").hasRole("ADMIN")
                // Officer management — ADMIN only
                .requestMatchers("/api/v1/customs/officers/**").hasRole("ADMIN")
                // Inspection — INSPECTOR or ADMIN
                .requestMatchers("/api/v1/customs/declarations/*/inspections").hasAnyRole("INSPECTOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/customs/declarations/*/hold").hasAnyRole("INSPECTOR", "ADMIN")
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
