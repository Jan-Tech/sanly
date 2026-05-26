package com.sanly.tax.exception;

import com.sanly.tax.dto.response.ErrorResponse;
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

@Slf4j @RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CitizenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCitizenNotFound(CitizenNotFoundException e, HttpServletRequest r) {
        return resp(HttpStatus.NOT_FOUND, "Not Found", e.getMessage(), r);
    }

    @ExceptionHandler(CitizenRegistryUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleRegistryDown(CitizenRegistryUnavailableException e, HttpServletRequest r) {
        return resp(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", e.getMessage(), r);
    }

    @ExceptionHandler(DuplicateTaxpayerException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateTaxpayerException e, HttpServletRequest r) {
        return resp(HttpStatus.CONFLICT, "Conflict", e.getMessage(), r);
    }

    @ExceptionHandler(InvalidFilingStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidFilingStateException e, HttpServletRequest r) {
        return resp(HttpStatus.BAD_REQUEST, "Invalid Filing State", e.getMessage(), r);
    }

    @ExceptionHandler({OfficerNotFoundException.class, TaxpayerNotFoundException.class, TaxFilingNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException e, HttpServletRequest r) {
        return resp(HttpStatus.NOT_FOUND, "Not Found", e.getMessage(), r);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest r) {
        Map<String, String> fe = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fe.put(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ErrorResponse.builder().status(400)
                .error("Validation Failed").message("Request validation failed").fieldErrors(fe).path(r.getRequestURI()).build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException e, HttpServletRequest r) {
        Map<String, String> fe = new LinkedHashMap<>();
        for (ConstraintViolation<?> cv : e.getConstraintViolations()) fe.put(cv.getPropertyPath().toString(), cv.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.builder().status(400)
                .error("Validation Failed").fieldErrors(fe).path(r.getRequestURI()).build());
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

    private ResponseEntity<ErrorResponse> resp(HttpStatus s, String error, String msg, HttpServletRequest r) {
        return ResponseEntity.status(s).body(ErrorResponse.builder()
                .status(s.value()).error(error).message(msg).path(r.getRequestURI()).build());
    }
}
