package com.sanly.customs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(RecordNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(e.getMessage(), 404));
    }

    @ExceptionHandler({InvalidOperationException.class, DuplicateResourceException.class})
    public ResponseEntity<Map<String, Object>> conflict(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(e.getMessage(), 409));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generic(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body("An unexpected error occurred", 500));
    }

    private Map<String, Object> body(String message, int status) {
        return Map.of("timestamp", LocalDateTime.now().toString(), "status", status, "error", message);
    }
}
