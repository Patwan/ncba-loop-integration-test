package com.example.apigateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Never reachable from outside. Blocked outright.
    private static final List<String> BLOCKED_PATHS = List.of(
            "/api/v1/auth/internal/",
            "/actuator/env",
            "/actuator/beans"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getPath().pathWithinApplication().value();

        // Block internal endpoints from the outside world.
        if (matches(path, BLOCKED_PATHS)) {
            return notFound(exchange);
        }

        // Everything else is public: pass through untouched.
        return chain.filter(exchange);
    }

    private static boolean matches(String path, List<String> prefixes) {
        return prefixes.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> notFound(ServerWebExchange ex) {
        return write(ex, HttpStatus.NOT_FOUND,
                Map.of("error", "Not Found", "source", "gateway"));
    }

    private Mono<Void> write(ServerWebExchange ex, HttpStatus status,
                             Map<String, String> body) {
        ServerHttpResponse res = ex.getResponse();
        res.setStatusCode(status);
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = MAPPER.writeValueAsBytes(body);
            return res.writeWith(Mono.just(res.bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            return res.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // After RouteToRequestUrlFilter (10000) so route metadata is populated,
        // and after CorsWebFilter, which runs earlier as a WebFilter.
        return RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1;
    }
}