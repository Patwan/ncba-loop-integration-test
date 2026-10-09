package org.example.crudservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CountryNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "COUNTRY_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(SoapException.class)
    public ResponseEntity<Map<String, Object>> handleSoap(SoapException ex) {
        return buildResponse(HttpStatus.BAD_GATEWAY, "SOAP_UPSTREAM_ERROR", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", code,
                "message", message
        ));
    }
}