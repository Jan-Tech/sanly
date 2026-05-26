package com.sanly.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Prevents clients from spoofing internal service headers.
 * These headers are set by the gateway's own JwtValidationFilter
 * or by inter-service communication — external callers must not inject them.
 */
@Component
public class ServiceHeaderStripFilter implements GlobalFilter, Ordered {

    private static final String[] INTERNAL_HEADERS = {
            "X-Citizen-NationalId",
            "X-Citizen-Roles",
            "X-Life-Event-Key",
            "X-Service-Name",
            "X-Service-Key",
            "X-Pharmacy-Code",
            "X-Pharmacy-Key"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
        for (String header : INTERNAL_HEADERS) {
            builder.headers(h -> h.remove(header));
        }
        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    @Override
    public int getOrder() {
        // Run BEFORE JwtValidationFilter so spoofed headers are stripped
        // before our filter adds the real ones.
        return Ordered.HIGHEST_PRECEDENCE - 1;
    }
}
