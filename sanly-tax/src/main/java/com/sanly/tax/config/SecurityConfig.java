package com.sanly.tax.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
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

@Configuration @EnableWebSecurity @EnableMethodSecurity @RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final LifeEventKeyFilter      lifeEventKeyFilter;
    private final AuthEntryPoint          authEntryPoint;
    private final RateLimitingFilter      rateLimitingFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**","/actuator/health","/swagger-ui/**","/v3/api-docs/**").permitAll()
                // Life-event internal endpoints — authenticated by LifeEventKeyFilter
                .requestMatchers("/api/v1/benefits/**").hasRole("LIFE_EVENT_SERVICE")
                .requestMatchers("/api/v1/taxpayers/update-marital-status").hasRole("LIFE_EVENT_SERVICE")
                .requestMatchers("/api/v1/taxpayers/deregister").hasRole("LIFE_EVENT_SERVICE")
                .anyRequest().authenticated())
            .addFilterBefore(rateLimitingFilter,   UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(lifeEventKeyFilter,   UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter,            UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
