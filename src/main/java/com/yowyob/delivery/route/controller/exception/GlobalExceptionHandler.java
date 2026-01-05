package com.yowyob.delivery.route.controller.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions pour l'application
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les erreurs de validation
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {
        
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Failed");
        
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value"
                ));
        
        errorResponse.put("fieldErrors", fieldErrors);
        
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    /**
     * Gère les erreurs d'accès aux données (R2DBC, conversion)
     */
    @ExceptionHandler(DataAccessException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleDataAccessException(
            DataAccessException ex,
            ServerWebExchange exchange) {
        
        log.error("Data access error: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Database Error");
        errorResponse.put("message", "An error occurred while accessing the database");
        
        // En développement, ajouter plus de détails
        if (log.isDebugEnabled()) {
            errorResponse.put("details", ex.getMessage());
        }
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    /**
     * Gère les erreurs de conversion de géométrie
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            ServerWebExchange exchange) {
        
        log.error("Illegal argument error: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Invalid Argument");
        errorResponse.put("message", ex.getMessage());
        
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    /**
     * Gère les ressources non trouvées
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            ServerWebExchange exchange) {
        
        log.warn("Resource not found: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());
        
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    /**
     * Gère le cas où le corps de la requête est mal formé (JSON invalide)
     */
    @ExceptionHandler(org.springframework.web.server.ServerWebInputException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleWebInputException(
            org.springframework.web.server.ServerWebInputException ex,
            ServerWebExchange exchange) {
        log.error("Malformed request body: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Malformed Request");
        errorResponse.put("message", "Malformed JSON or invalid request body: " + ex.getMessage());

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    /**
     * Gère les cas où aucun chemin n'a pu être trouvé
     */
    @ExceptionHandler(com.yowyob.delivery.route.controller.exception.NoPathFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNoPathFoundException(
            com.yowyob.delivery.route.controller.exception.NoPathFoundException ex,
            ServerWebExchange exchange) {
        log.warn("No path found: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        errorResponse.put("error", "Unprocessable Entity");
        errorResponse.put("message", ex.getMessage());

        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse));
    }

    /**
     * Gère toutes les autres exceptions non prévues
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(
            Exception ex,
            ServerWebExchange exchange) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An unexpected error occurred");
        
        // En développement, ajouter la stack trace
        if (log.isDebugEnabled()) {
            errorResponse.put("details", ex.getMessage());
            errorResponse.put("exception", ex.getClass().getName());
        }
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }
}