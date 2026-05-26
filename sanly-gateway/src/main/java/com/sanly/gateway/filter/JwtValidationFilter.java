package com.sanly.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Soft JWT validation: if a valid citizen-registry JWT is present, add
 * X-Citizen-NationalId and X-Citizen-Roles headers for downstream services.
 * If no token or an invalid token is present, the request passes through
 * without those headers — downstream services enforce their own auth.
 */
@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey signingKey;

    public JwtValidationFilter(@Value("${jwt.secret}") String jwtSecret) {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String nationalId = claims.getSubject();
            Object rolesObj = claims.get("roles");
            String roles = rolesObj != null ? rolesObj.toString() : "";

            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("X-Citizen-NationalId", nationalId)
                    .header("X-Citizen-Roles", roles)
                    .build();

            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            // Non-registry JWT (doctor/officer JWT) or invalid — pass through without citizen headers
            log.debug("JWT not validated as citizen-registry token: {}", e.getMessage());
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
