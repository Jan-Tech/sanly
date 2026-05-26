package com.sanly.customs.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final long capacity;
    private final long refill;

    public RateLimitingFilter(@Value("${rate-limit.capacity:100}") long capacity,
                               @Value("${rate-limit.refill-tokens:100}") long refill) {
        this.capacity = capacity; this.refill = refill;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        Bucket bucket = cache.computeIfAbsent(req.getRemoteAddr(), k -> Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.greedy(refill, Duration.ofMinutes(1))))
                .build());
        if (bucket.tryConsume(1)) { chain.doFilter(req, res); }
        else { res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); res.getWriter().write("Too many requests"); }
    }
}
