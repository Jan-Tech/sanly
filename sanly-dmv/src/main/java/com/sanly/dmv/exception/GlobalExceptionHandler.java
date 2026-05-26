package com.sanly.dmv.exception;

import com.sanly.dmv.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OfficerNotFoundException.class)
    public ResponseEntity<ErrorResponse> h(OfficerNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ErrorResponse> h(ApplicationNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(DrivingLicenseNotFoundException.class)
    public ResponseEntity<ErrorResponse> h(DrivingLicenseNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(CitizenNotFoundException.class)
    public ResponseEntity<ErrorResponse> h(CitizenNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "Citizen Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(ApplicationNotApprovedException.class)
    public ResponseEntity<ErrorResponse> h(ApplicationNotApprovedException ex, HttpServletRequest req) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "Application Not Approved", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateLicenseException.class)
    public ResponseEntity<ErrorResponse> h(DuplicateLicenseException ex, HttpServletRequest req) {
        return response(HttpStatus.CONFLICT, "Duplicate License", ex.getMessage(), req);
    }

    // ===== Vision Test failures — 422 Unprocessable Entity =====

    @ExceptionHandler(VisionTestRequiredException.class)
    public ResponseEntity<ErrorResponse> h(VisionTestRequiredException ex, HttpServletRequest req) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "Vision Test Required", ex.getMessage(), req);
    }

    @ExceptionHandler(VisionTestExpiredException.class)
    public ResponseEntity<ErrorResponse> h(VisionTestExpiredException ex, HttpServletRequest req) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "Vision Test Expired", ex.getMessage(), req);
    }

    @ExceptionHandler(VisionTestFailedException.class)
    public ResponseEntity<ErrorResponse> h(VisionTestFailedException ex, HttpServletRequest req) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "Vision Test Failed", ex.getMessage(), req);
    }

    // ===== Integration failures =====

    @ExceptionHandler(CitizenRegistryUnavailableException.class)
    public ResponseEntity<ErrorResponse> h(CitizenRegistryUnavailableException ex, HttpServletRequest req) {
        log.warn("Citizen Registry unavailable: {}", ex.getMessage());
        return response(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", ex.getMessage(), req);
    }

    @ExceptionHandler(BridgeUnavailableException.class)
    public ResponseEntity<ErrorResponse> h(BridgeUnavailableException ex, HttpServletRequest req) {
        log.warn("SANLY Bridge unavailable: {}", ex.getMessage());
        return response(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", ex.getMessage(), req);
    }

    @ExceptionHandler(InsufficientPermissionException.class)
    public ResponseEntity<ErrorResponse> h(InsufficientPermissionException ex, HttpServletRequest req) {
        log.error("Bridge permission denied: {}", ex.getMessage());
        return response(HttpStatus.FORBIDDEN, "Insufficient Bridge Permission", ex.getMessage(), req);
    }

    // ===== Validation =====

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> h(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .status(400).error("Validation Failed").message("One or more fields failed validation")
                .fieldErrors(fieldErrors).path(req.getRequestURI()).build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> h(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations())
            fieldErrors.put(cv.getPropertyPath().toString(), cv.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .status(400).error("Validation Failed").message("Parameter validation failed")
                .fieldErrors(fieldErrors).path(req.getRequestURI()).build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> h(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, "Malformed Request",
                "Request body is missing or cannot be parsed", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> h(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, "Type Mismatch",
                "Parameter '" + ex.getName() + "' has an invalid value", req);
    }

    // ===== Security =====

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> h(AccessDeniedException ex, HttpServletRequest req) {
        return response(HttpStatus.FORBIDDEN, "Access Denied",
                "You do not have permission to perform this action", req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> h(BadCredentialsException ex, HttpServletRequest req) {
        return response(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid username or password", req);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> h(DisabledException ex, HttpServletRequest req) {
        return response(HttpStatus.UNAUTHORIZED, "Account Disabled",
                "Account disabled. Contact an administrator.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> h(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", req);
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String error,
                                                    String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .status(status.value()).error(error).message(message)
                .path(req.getRequestURI()).build());
    }
}
