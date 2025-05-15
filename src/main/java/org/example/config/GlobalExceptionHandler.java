package org.example.config;

import org.example.exception.ApiException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        Map<String, Object> body = Map.of(
                "code", ex.getCode(),
                "message", ex.getMessage(),
                "status", ex.getStatus(),
                "timestamp", Instant.now()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }
}