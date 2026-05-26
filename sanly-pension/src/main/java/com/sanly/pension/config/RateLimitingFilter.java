package com.sanly.pension.config;

import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final long capacity;
    private final long refillTokens;
    private final long refillMinutes;

    public RateLimitingFilter(
            @Value("${rate-limit.capacity:100}") long capacity,
            @Value("${rate-limit.refill-tokens:100}") long refillTokens,
            @Value("${rate-limit.refill-duration-minutes:1}") long refillMinutes) {
        this.capacity = capacity; this.refillTokens = refillTokens; this.refillMinutes = refillMinutes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Bucket bucket = buckets.computeIfAbsent(req.getRemoteAddr(), k -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(capacity)
                        .refillGreedy(refillTokens, Duration.ofMinutes(refillMinutes)).build())
                .build());
        if (bucket.tryConsume(1)) { chain.doFilter(req, res); }
        else { res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); res.getWriter().write("{\"error\":\"Too many requests\"}"); }
    }
}
