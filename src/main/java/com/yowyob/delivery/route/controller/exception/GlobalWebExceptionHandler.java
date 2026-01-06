package com.yowyob.delivery.route.controller.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * A low-level WebExceptionHandler that guarantees JSON error bodies for any
 * unhandled exception in the WebFlux stack (including filter/codec errors).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        // Determine status
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An unexpected error occurred";

        if (ex instanceof ResponseStatusException rse) {
            // ResponseStatusException#getStatusCode() returns HttpStatusCode in newer Spring
            // Convert to HttpStatus safely
            try {
                int code = rse.getStatusCode().value();
                HttpStatus resolved = HttpStatus.resolve(code);
                status = resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
            } catch (Exception ignore) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            message = rse.getReason() != null ? rse.getReason() : rse.getMessage();
        } else if (ex instanceof NoPathFoundException npf) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
            message = npf.getMessage();
        } else if (ex instanceof IllegalArgumentException iae) {
            status = HttpStatus.BAD_REQUEST;
            message = iae.getMessage();
        }

        log.warn("GlobalWebExceptionHandler handling error: {} {} -> {}", exchange.getRequest().getPath().value(), status, ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("path", exchange.getRequest().getPath().value());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message != null ? message : ex.getMessage());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to serialize error body", e);
            bytes = ("{\"message\":\"Internal serialization error\"}").getBytes(StandardCharsets.UTF_8);
        }

        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().setStatusCode(status);

        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}
