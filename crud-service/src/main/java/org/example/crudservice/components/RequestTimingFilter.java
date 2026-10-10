package org.example.crudservice.components;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTimingFilter extends OncePerRequestFilter {

    public static final String REQUEST_TIME_ATTR = "requestStartTime";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Instant start = Instant.now();
        request.setAttribute(REQUEST_TIME_ATTR, start);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = Instant.now().toEpochMilli() - start.toEpochMilli();
            response.setHeader("X-Response-Time-Ms", String.valueOf(durationMs));
        }
    }
}