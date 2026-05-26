package com.sanly.bridge.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sanly.bridge.dto.response.ErrorResponse;
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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dual-mode rate limiter:
 * <ul>
 *   <li>Exchange paths — keyed by institution code (50 req/min)</li>
 *   <li>All other paths — keyed by client IP (100 req/min)</li>
 * </ul>
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String EXCHANGE_PATH = "/api/v1/exchange";

    private final int adminCapacity;
    private final int adminRefill;
    private final long adminDurationMin;

    private final int instCapacity;
    private final int instRefill;
    private final long instDurationMin;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper   = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public RateLimitingFilter(
            @Value("${rate-limit.admin.capacity}")               int adminCapacity,
            @Value("${rate-limit.admin.refill-tokens}")          int adminRefill,
            @Value("${rate-limit.admin.refill-duration-minutes}")long adminDurationMin,
            @Value("${rate-limit.institution.capacity}")               int instCapacity,
            @Value("${rate-limit.institution.refill-tokens}")          int instRefill,
            @Value("${rate-limit.institution.refill-duration-minutes}")long instDurationMin) {
        this.adminCapacity    = adminCapacity;
        this.adminRefill      = adminRefill;
        this.adminDurationMin = adminDurationMin;
        this.instCapacity     = instCapacity;
        this.instRefill       = instRefill;
        this.instDurationMin  = instDurationMin;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri.startsWith("/actuator") || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")) {
            chain.doFilter(request, response);
            return;
        }

        boolean isExchange = uri.startsWith(EXCHANGE_PATH);
        String bucketKey;
        Bucket bucket;

        if (isExchange) {
            String code = request.getHeader("X-Institution-Code");
            bucketKey = StringUtils.hasText(code)
                    ? "inst:" + code
                    : "ip:" + clientIp(request);
            bucket = buckets.computeIfAbsent(bucketKey,
                    k -> newBucket(instCapacity, instRefill, instDurationMin));
        } else {
            bucketKey = "ip:" + clientIp(request);
            bucket = buckets.computeIfAbsent(bucketKey,
                    k -> newBucket(adminCapacity, adminRefill, adminDurationMin));
        }

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for key: {}", bucketKey);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse body = ErrorResponse.builder()
                    .status(429)
                    .error("Too Many Requests")
                    .message("Rate limit exceeded. Please slow down.")
                    .path(uri)
                    .build();
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }

    private Bucket newBucket(int capacity, int refill, long minutes) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(refill, Duration.ofMinutes(minutes))
                        .build())
                .build();
    }

    private String clientIp(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(h -> h.split(",")[0].trim())
                .orElse(request.getRemoteAddr());
    }
}
