package com.sanly.banking.controller;

import com.sanly.banking.dto.request.ApproveConsentRequest;
import com.sanly.banking.dto.request.ConsentRequestDto;
import com.sanly.banking.dto.response.*;
import com.sanly.banking.entity.BankDataAccess;
import com.sanly.banking.exception.RateLimitExceededException;
import com.sanly.banking.service.ConsentService;
import com.sanly.banking.service.DataAccessService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/banking/consent")
@RequiredArgsConstructor
@Tag(name = "Consent", description = "Consent request and management endpoints")
public class ConsentController {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ConsentService  consentService;
    private final DataAccessService dataAccessService;

    // ===== Bucket4j rate limiting per bankCode (20 consent requests per bank per hour) =====
    private final ConcurrentHashMap<String, Bucket> bankBuckets = new ConcurrentHashMap<>();

    private Bucket getBucket(String bankCode) {
        return bankBuckets.computeIfAbsent(bankCode, k ->
                Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(20)
                                .refillIntervally(20, Duration.ofHours(1))
                                .build())
                        .build());
    }

    // ===================== Bank-facing (no JWT, API key in headers) =====================

    @PostMapping("/request")
    @Operation(summary = "Bank requests citizen consent",
               description = "Bank initiates a consent request. Citizen receives SMS notification. " +
                             "Authenticated via X-Bank-Code + X-Bank-Key headers. " +
                             "Rate limited: 20 requests per bank per hour.")
    public ResponseEntity<ApiResponse<ConsentRequestResponse>> requestConsent(
            @RequestHeader("X-Bank-Code") String bankCode,
            @RequestHeader("X-Bank-Key")  String bankKey,
            @Valid @RequestBody ConsentRequestDto request) {

        // Rate limit check
        Bucket bucket = getBucket(bankCode);
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Rate limit exceeded for bank " + bankCode +
                    ". Maximum 20 consent requests per hour.");
        }

        var consent = consentService.requestConsent(
                bankCode, bankKey,
                request.citizenNationalId(),
                request.requestedScopes(),
                request.purpose());

        // Build minimal response (bank doesn't need all fields)
        ConsentRequestResponse response = ConsentRequestResponse.builder()
                .consentCode(consent.getConsentCode())
                .bankCode(consent.getBankCode())
                .status(consent.getStatus())
                .requestedAt(consent.getRequestedAt() != null ? consent.getRequestedAt().format(DT_FMT) : null)
                .expiresAt(consent.getExpiresAt() != null ? consent.getExpiresAt().format(DT_FMT) : null)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Consent request submitted. Citizen will receive an SMS notification.", response));
    }

    @GetMapping("/{consentCode}/status")
    @Operation(summary = "Bank polls consent status",
               description = "Bank polls for consent approval. Returns plain token once when APPROVED — " +
                             "token is cleared from cache after this call. " +
                             "No authentication required (consent code is a secret).")
    public ResponseEntity<ApiResponse<ConsentStatusResponse>> getConsentStatus(
            @PathVariable String consentCode) {

        ConsentStatusResponse status = consentService.getConsentStatus(consentCode);
        return ResponseEntity.ok(ApiResponse.ok(status));
    }

    // ===================== Citizen-facing (JWT required) =====================

    @GetMapping("/pending")
    @Operation(summary = "Get pending consents for citizen",
               description = "Returns all pending consent requests for the authenticated citizen.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<ConsentRequestResponse>>> getPendingConsents(
            Authentication auth) {

        String citizenNationalId = extractNationalId(auth);
        List<ConsentRequestResponse> pending = consentService.getPendingForCitizen(citizenNationalId);
        return ResponseEntity.ok(ApiResponse.ok(pending));
    }

    @GetMapping("/{consentCode}")
    @Operation(summary = "Get consent details for citizen")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ConsentRequestResponse>> getConsentDetails(
            @PathVariable String consentCode,
            Authentication auth) {

        String citizenNationalId = extractNationalId(auth);
        ConsentRequestResponse details = consentService.getConsentDetails(consentCode, citizenNationalId);
        return ResponseEntity.ok(ApiResponse.ok(details));
    }

    @PostMapping("/{consentCode}/approve")
    @Operation(summary = "Citizen approves consent",
               description = "Citizen approves consent with OTP verification. " +
                             "Bank can then retrieve the token via GET /consent/{code}/status.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> approveConsent(
            @PathVariable String consentCode,
            @Valid @RequestBody ApproveConsentRequest request,
            Authentication auth) {

        String citizenNationalId = extractNationalId(auth);
        consentService.approveConsent(consentCode, citizenNationalId,
                request.approvedScopes(), request.otpCode());

        return ResponseEntity.ok(ApiResponse.ok("Consent approved successfully. Bank may now access your data.", null));
    }

    @PostMapping("/{consentCode}/reject")
    @Operation(summary = "Citizen rejects consent")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> rejectConsent(
            @PathVariable String consentCode,
            Authentication auth) {

        String citizenNationalId = extractNationalId(auth);
        consentService.rejectConsent(consentCode, citizenNationalId);

        return ResponseEntity.ok(ApiResponse.ok("Consent rejected.", null));
    }

    // ===================== Citizen access history =====================

    @GetMapping("/history")
    @Operation(summary = "Get citizen's data access history (alias)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Page<BankDataAccess>>> getAccessHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {

        String citizenNationalId = extractNationalId(auth);
        Page<BankDataAccess> history = dataAccessService.getAccessHistoryForCitizen(
                citizenNationalId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    // ===== Helpers =====

    private String extractNationalId(Authentication auth) {
        if (auth.getPrincipal() instanceof String) {
            return (String) auth.getPrincipal(); // citizen token
        }
        throw new IllegalStateException("Citizen authentication required");
    }
}
