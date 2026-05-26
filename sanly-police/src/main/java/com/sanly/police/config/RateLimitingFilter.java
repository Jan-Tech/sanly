package com.sanly.police.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sanly.police.dto.response.ErrorResponse;
import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final int capacity, refill;
    private final long refillMinutes;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

    public RateLimitingFilter(@Value("${rate-limit.capacity}") int capacity,
                              @Value("${rate-limit.refill-tokens}") int refill,
                              @Value("${rate-limit.refill-duration-minutes}") long refillMinutes) {
        this.capacity = capacity; this.refill = refill; this.refillMinutes = refillMinutes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri.startsWith("/actuator") || uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")) {
            chain.doFilter(req, res); return;
        }
        String ip = Optional.ofNullable(req.getHeader("X-Forwarded-For"))
                .map(h -> h.split(",")[0].trim()).orElse(req.getRemoteAddr());
        Bucket bucket = buckets.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(capacity)
                        .refillGreedy(refill, Duration.ofMinutes(refillMinutes)).build())
                .build());
        if (bucket.tryConsume(1)) { chain.doFilter(req, res); return; }
        res.setStatus(429);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(om.writeValueAsString(ErrorResponse.builder()
                .status(429).error("Too Many Requests")
                .message("Rate limit exceeded").path(uri).build()));
    }
}
