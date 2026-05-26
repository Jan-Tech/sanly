package com.sanly.land.config;

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
    private final LifeEventKeyFilter      lifeEventKeyFilter;
    private final RateLimitingFilter      rateLimitingFilter;
    private final AuthEntryPoint          authEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, LifeEventKeyFilter lifeEventKeyFilter,
                          RateLimitingFilter rateLimitingFilter, AuthEntryPoint authEntryPoint) {
        this.jwtFilter          = jwtFilter;
        this.lifeEventKeyFilter = lifeEventKeyFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.authEntryPoint     = authEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(authEntryPoint))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/v1/land/auth/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/land/properties/verify/**").permitAll()
                .requestMatchers("/api/v1/land/ownership/deceased").hasRole("LIFE_EVENT_SERVICE")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/land/properties/*/status").hasRole("ADMIN")
                .requestMatchers("/api/v1/land/officers/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(rateLimitingFilter,  UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(lifeEventKeyFilter,  UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter,           UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }
}
