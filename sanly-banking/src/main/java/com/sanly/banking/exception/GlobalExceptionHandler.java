package com.sanly.banking.exception;

import com.sanly.banking.dto.response.ErrorResponse;
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

    // ===== Domain =====

    @ExceptionHandler(BankNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBankNotFound(BankNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(BankSuspendedException.class)
    public ResponseEntity<ErrorResponse> handleBankSuspended(BankSuspendedException ex, HttpServletRequest req) {
        return response(HttpStatus.FORBIDDEN, "Bank Suspended", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidApiKey(InvalidApiKeyException ex, HttpServletRequest req) {
        return response(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req);
    }

    @ExceptionHandler(ConsentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConsentNotFound(ConsentNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(ConsentExpiredException.class)
    public ResponseEntity<ErrorResponse> handleConsentExpired(ConsentExpiredException ex, HttpServletRequest req) {
        return response(HttpStatus.GONE, "Consent Expired", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex, HttpServletRequest req) {
        return response(HttpStatus.UNAUTHORIZED, "Invalid Token", ex.getMessage(), req);
    }

    @ExceptionHandler(CitizenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCitizenNotFound(CitizenNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "Citizen Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(OtpVerificationException.class)
    public ResponseEntity<ErrorResponse> handleOtpFailed(OtpVerificationException ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, "OTP Verification Failed", ex.getMessage(), req);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RateLimitExceededException ex, HttpServletRequest req) {
        return response(HttpStatus.TOO_MANY_REQUESTS, "Rate Limit Exceeded", ex.getMessage(), req);
    }

    // ===== Validation =====

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));
        ErrorResponse body = ErrorResponse.builder()
                .status(400).error("Validation Failed")
                .message("One or more fields failed validation")
                .fieldErrors(fieldErrors)
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            fieldErrors.put(cv.getPropertyPath().toString(), cv.getMessage());
        }
        ErrorResponse body = ErrorResponse.builder()
                .status(400).error("Validation Failed")
                .message("Parameter validation failed")
                .fieldErrors(fieldErrors)
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.badRequest().body(body);
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

    // ===== Security =====

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return response(HttpStatus.FORBIDDEN, "Access Denied",
                "You do not have permission to perform this action", req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return response(HttpStatus.UNAUTHORIZED, "Unauthorized",
                "Invalid username or password", req);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex, HttpServletRequest req) {
        return response(HttpStatus.UNAUTHORIZED, "Account Disabled",
                "This account has been disabled", req);
    }

    // ===== Catch-all =====

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", req);
    }

    // ---- helper ----

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String error, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .status(status.value())
                        .error(error)
                        .message(message)
                        .path(req.getRequestURI())
                        .build());
    }
}
