package com.sanly.registry.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sanly.registry.dto.response.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate limiting using Bucket4j in-memory buckets.
 *
 * Each unique IP gets its own token bucket. A single shared bucket is used for
 * requests from private/loopback addresses (test/dev convenience).
 * In production, back the buckets with Redis for multi-instance support.
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final int      capacity;
    private final int      refillTokens;
    private final long     refillMinutes;
    private final ObjectMapper objectMapper;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${rate-limit.capacity}") int capacity,
            @Value("${rate-limit.refill-tokens}") int refillTokens,
            @Value("${rate-limit.refill-duration-minutes}") long refillMinutes) {
        this.capacity      = capacity;
        this.refillTokens  = refillTokens;
        this.refillMinutes = refillMinutes;
        this.objectMapper  = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {

        // Skip rate limiting for actuator/health and Swagger paths
        String uri = request.getRequestURI();
        if (uri.startsWith("/actuator") || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {}", ip);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("X-RateLimit-Retry-After", String.valueOf(refillMinutes * 60));

            ErrorResponse body = ErrorResponse.builder()
                    .status(429)
                    .error("Too Many Requests")
                    .message("Rate limit exceeded. Please try again later.")
                    .path(uri)
                    .build();
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, Duration.ofMinutes(refillMinutes))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String clientIp(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(h -> h.split(",")[0].trim())
                .orElse(request.getRemoteAddr());
    }
}
