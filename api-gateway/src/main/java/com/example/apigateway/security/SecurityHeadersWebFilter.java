package com.example.apigateway.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersWebFilter implements WebFilter {

    private static final String CSP_STRICT =
            "default-src 'self'; script-src 'self'; style-src 'self'; " +
                    "img-src 'self' data:; font-src 'self'; frame-ancestors 'none'; " +
                    "base-uri 'self'; form-action 'self'";

    private static final String CSP_SWAGGER =
            "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                    "style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; " +
                    "font-src 'self' data:; frame-ancestors 'none'; " +
                    "base-uri 'self'; form-action 'self'";

    // Paths whose OpenAPI/Swagger UI must use the relaxed CSP.
    private static final List<String> SWAGGER_PREFIXES = List.of(
            "/swagger-ui",
            "/v3/api-docs"
    );

    // Auth paths whose responses must never be cached.
    private static final List<String> NO_CACHE_PREFIXES = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/resend-otp",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getResponse().beforeCommit(() -> {
            HttpHeaders h = exchange.getResponse().getHeaders();
            String path = exchange.getRequest().getPath().value();

            h.set("X-Frame-Options", "DENY");
            h.set("X-Content-Type-Options", "nosniff");
            h.set("Referrer-Policy", "strict-origin-when-cross-origin");
            h.set("Permissions-Policy",
                    "geolocation=(), camera=(), microphone=(), payment=()");
            h.remove("Server");

            boolean swagger = matches(path, SWAGGER_PREFIXES)
                    || path.endsWith("/api-docs");
            h.set("Content-Security-Policy", swagger ? CSP_SWAGGER : CSP_STRICT);

            if (isSecure(exchange)) {
                h.set("Strict-Transport-Security",
                        "max-age=31536000; includeSubDomains; preload");
            }

            if (matches(path, NO_CACHE_PREFIXES)) {
                h.set(HttpHeaders.CACHE_CONTROL,
                        "no-store, no-cache, must-revalidate, private");
                h.set(HttpHeaders.PRAGMA, "no-cache");
                h.set(HttpHeaders.EXPIRES, "0");
            }
            return Mono.empty();
        });
        return chain.filter(exchange);
    }

    private static boolean matches(String path, List<String> prefixes) {
        return prefixes.stream().anyMatch(path::startsWith);
    }

    private boolean isSecure(ServerWebExchange ex) {
        String proto = ex.getRequest().getHeaders().getFirst("X-Forwarded-Proto");
        if (proto != null) return "https".equalsIgnoreCase(proto);
        return "https".equalsIgnoreCase(ex.getRequest().getURI().getScheme());
    }
}