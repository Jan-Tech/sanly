package com.sanly.banking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final AuthEntryPoint          authEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
            .authorizeHttpRequests(auth -> auth
                // Auth
                .requestMatchers(HttpMethod.POST, "/api/v1/banking/auth/login").permitAll()
                // Swagger & Actuator
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Bank-facing endpoints (API key validated in controller, no JWT required)
                .requestMatchers(HttpMethod.POST, "/api/v1/banking/consent/request").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/banking/consent/*/status").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/banking/citizen/profile").permitAll()
                // Citizen JWT endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/banking/consent/pending").hasAnyRole("CITIZEN", "OFFICER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/banking/consent/*").hasAnyRole("CITIZEN", "OFFICER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/banking/consent/*/approve").hasAnyRole("CITIZEN")
                .requestMatchers(HttpMethod.POST, "/api/v1/banking/consent/*/reject").hasAnyRole("CITIZEN")
                .requestMatchers(HttpMethod.GET, "/api/v1/banking/my/**").hasAnyRole("CITIZEN")
                // Admin endpoints
                .requestMatchers("/api/v1/banking/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/banking/banks/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
