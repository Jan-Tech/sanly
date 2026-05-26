package com.sanly.gateway.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final Map<String, Bucket> globalBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    private final long globalCapacity;
    private final long globalRefill;
    private final long authCapacity;
    private final long authRefill;

    public RateLimitFilter(
            @Value("${rate-limit.global-capacity:200}") long globalCapacity,
            @Value("${rate-limit.global-refill-tokens:200}") long globalRefill,
            @Value("${rate-limit.auth-capacity:20}") long authCapacity,
            @Value("${rate-limit.auth-refill-tokens:20}") long authRefill) {
        this.globalCapacity = globalCapacity;
        this.globalRefill = globalRefill;
        this.authCapacity = authCapacity;
        this.authRefill = authRefill;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = resolveIp(exchange);
        String path = exchange.getRequest().getPath().value();

        boolean isAuthPath = path.contains("/auth/login") || path.contains("/auth/register");

        Bucket bucket = isAuthPath
                ? authBuckets.computeIfAbsent(ip, k -> buildBucket(authCapacity, authRefill))
                : globalBuckets.computeIfAbsent(ip, k -> buildBucket(globalCapacity, globalRefill));

        if (bucket.tryConsume(1)) {
            return chain.filter(exchange);
        }

        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("X-RateLimit-RetryAfter", "60");
        return exchange.getResponse().setComplete();
    }

    private Bucket buildBucket(long capacity, long refillTokens) {
        Bandwidth limit = Bandwidth.classic(capacity,
                Refill.greedy(refillTokens, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress addr = exchange.getRequest().getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
