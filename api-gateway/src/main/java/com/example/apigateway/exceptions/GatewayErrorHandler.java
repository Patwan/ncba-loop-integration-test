package com.example.apigateway.exceptions;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webflux.autoconfigure.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.Map;

@Component
@Order(-2)   // ahead of DefaultErrorWebExceptionHandler, which sits at -1
public class GatewayErrorHandler extends AbstractErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayErrorHandler.class);

    public GatewayErrorHandler(ErrorAttributes errorAttributes,
                               WebProperties webProperties,
                               ApplicationContext applicationContext,
                               ServerCodecConfigurer codecConfigurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        super.setMessageWriters(codecConfigurer.getWriters());
        super.setMessageReaders(codecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::render);
    }

    private Mono<ServerResponse> render(ServerRequest request) {
        Throwable error = getError(request);

        HttpStatus status;
        String message;

        if (error instanceof ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service temporarily unavailable. Please try again.";

        } else if (error instanceof java.util.concurrent.TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "The request took too long. Please try again.";

        } else if (error instanceof ResponseStatusException rse) {
            // Gateway's NotFoundException extends this and already carries 503.
            HttpStatusCode code = rse.getStatusCode();
            status = HttpStatus.resolve(code.value()) != null
                    ? HttpStatus.valueOf(code.value())
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            message = switch (status) {
                case SERVICE_UNAVAILABLE -> "Service temporarily unavailable. Please try again.";
                case GATEWAY_TIMEOUT -> "The request took too long. Please try again.";
                case NOT_FOUND -> "Resource not found.";
                default -> status.getReasonPhrase();
            };

        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred.";
        }

        // Detail stays in the log, never goes to the client.
        log.error("Gateway error on {} {}: {}",
                request.method(), request.path(), error.toString());

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "error", message,
                        "source", "ncba-loop-test-by-martin-ndungu",
                        "path", request.path()));
    }
}