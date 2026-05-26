package com.sanly.appointments.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
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
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
    private final long capacity;
    private final long refillMinutes;

    public RateLimitingFilter(
            @Value("${rate-limit.capacity:100}") long capacity,
            @Value("${rate-limit.refill-minutes:1}") long refillMinutes) {
        this.capacity = capacity;
        this.refillMinutes = refillMinutes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);

        // Skip rate limiting for internal/loopback requests
        if (isInternalRequest(clientIp)) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = ipBuckets.computeIfAbsent(clientIp, this::createBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"success\":false,\"message\":\"Rate limit exceeded. Please try again later.\"}");
        }
    }

    private Bucket createBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(capacity,
                Refill.greedy(capacity, Duration.ofMinutes(refillMinutes)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isInternalRequest(String ip) {
        return ip != null && (ip.equals("127.0.0.1") || ip.equals("::1") || ip.startsWith("172.") || ip.startsWith("10."));
    }
}
