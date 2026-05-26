package com.sanly.police.exception;

import com.sanly.police.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CitizenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCitizenNotFound(CitizenNotFoundException e, HttpServletRequest r) {
        return resp(HttpStatus.NOT_FOUND, "Not Found", e.getMessage(), r);
    }

    @ExceptionHandler(CitizenRegistryUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleRegistryDown(CitizenRegistryUnavailableException e, HttpServletRequest r) {
        return resp(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", e.getMessage(), r);
    }

    @ExceptionHandler(BridgeUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleBridgeDown(BridgeUnavailableException e, HttpServletRequest r) {
        return resp(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", e.getMessage(), r);
    }

    @ExceptionHandler(InsufficientPermissionException.class)
    public ResponseEntity<ErrorResponse> handleInsufficient(InsufficientPermissionException e, HttpServletRequest r) {
        return resp(HttpStatus.FORBIDDEN, "Permission Denied", e.getMessage(), r);
    }

    @ExceptionHandler({OfficerNotFoundException.class, CriminalRecordNotFoundException.class,
                        CitizenCheckNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException e, HttpServletRequest r) {
        return resp(HttpStatus.NOT_FOUND, "Not Found", e.getMessage(), r);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest r) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        ErrorResponse body = ErrorResponse.builder().status(400).error("Validation Failed")
                .message("Request validation failed").fieldErrors(fieldErrors).path(r.getRequestURI()).build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException e, HttpServletRequest r) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> cv : e.getConstraintViolations())
            fieldErrors.put(cv.getPropertyPath().toString(), cv.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.builder().status(400)
                .error("Validation Failed").fieldErrors(fieldErrors).path(r.getRequestURI()).build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccess(AccessDeniedException e, HttpServletRequest r) {
        return resp(HttpStatus.FORBIDDEN, "Access Denied", "You do not have permission to perform this action", r);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCred(BadCredentialsException e, HttpServletRequest r) {
        return resp(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid username or password", r);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e, HttpServletRequest r) {
        log.error("Unhandled exception at {}: {}", r.getRequestURI(), e.getMessage(), e);
        return resp(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", r);
    }

    private ResponseEntity<ErrorResponse> resp(HttpStatus status, String error, String message, HttpServletRequest r) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .status(status.value()).error(error).message(message).path(r.getRequestURI()).build());
    }
}
