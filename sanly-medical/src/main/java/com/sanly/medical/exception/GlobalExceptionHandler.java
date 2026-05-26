package com.sanly.medical.exception;

import com.sanly.medical.dto.response.ErrorResponse;
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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClinicNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClinicNotFound(ClinicNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDoctorNotFound(DoctorNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(MedicalRecordNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRecordNotFound(MedicalRecordNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(CitizenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCitizenNotFound(CitizenNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "Citizen Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(CitizenRegistryUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleRegistryUnavailable(CitizenRegistryUnavailableException ex, HttpServletRequest req) {
        log.warn("Citizen Registry unavailable: {}", ex.getMessage());
        return response(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", ex.getMessage(), req);
    }

    @ExceptionHandler(ClinicSuspendedException.class)
    public ResponseEntity<ErrorResponse> handleClinicSuspended(ClinicSuspendedException ex, HttpServletRequest req) {
        return response(HttpStatus.FORBIDDEN, "Clinic Suspended", ex.getMessage(), req);
    }

    @ExceptionHandler(ClinicAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleClinicAccess(ClinicAccessDeniedException ex, HttpServletRequest req) {
        return response(HttpStatus.FORBIDDEN, "Access Denied", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateLicenseException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateLicenseException ex, HttpServletRequest req) {
        return response(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .status(400).error("Validation Failed")
                .message("One or more fields failed validation")
                .fieldErrors(fieldErrors).path(req.getRequestURI()).build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations())
            fieldErrors.put(cv.getPropertyPath().toString(), cv.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .status(400).error("Validation Failed")
                .message("Parameter validation failed")
                .fieldErrors(fieldErrors).path(req.getRequestURI()).build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, "Malformed Request",
                "Request body is missing or cannot be parsed", req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, "Missing Parameter",
                "Required parameter '" + ex.getParameterName() + "' is missing", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, "Type Mismatch",
                "Parameter '" + ex.getName() + "' has an invalid value", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return response(HttpStatus.FORBIDDEN, "Access Denied",
                "You do not have permission to perform this action", req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return response(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid username or password", req);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex, HttpServletRequest req) {
        return response(HttpStatus.UNAUTHORIZED, "Account Disabled",
                "Your account has been disabled. Contact an administrator.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
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
