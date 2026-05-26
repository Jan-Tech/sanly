package com.sanly.signature.controller;

import com.sanly.signature.config.UserDetailsImpl;
import com.sanly.signature.dto.response.ApiResponse;
import com.sanly.signature.dto.response.VerifyResponse;
import com.sanly.signature.service.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/signature/verify")
public class VerifyController {

    private final VerificationService verificationService;

    public VerifyController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    /**
     * Full verification: upload the document alongside the signature code to verify
     * both document integrity (SHA-256 hash match) and the HMAC-SHA256 signature value.
     * No authentication required — anyone can verify a public signature.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VerifyResponse>> verifyWithDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("signatureCode") String signatureCode,
            HttpServletRequest httpRequest) {

        String verifierIp = httpRequest.getRemoteAddr();
        String verifierNationalId = resolveVerifierNationalId();

        VerifyResponse response = verificationService.verifyWithDocument(
                file, signatureCode, verifierIp, verifierNationalId);

        String message = response.isValid()
                ? "Document signature is valid"
                : "Signature verification failed: " + response.getMessage();

        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    /**
     * Metadata-only verification by signature code. No document upload required.
     * Returns signature metadata and status without re-verifying the document hash.
     * No authentication required — QR code scanners use this endpoint.
     */
    @GetMapping("/{signatureCode}")
    public ResponseEntity<ApiResponse<VerifyResponse>> verifyByCode(
            @PathVariable String signatureCode,
            HttpServletRequest httpRequest) {

        String verifierIp = httpRequest.getRemoteAddr();
        String verifierNationalId = resolveVerifierNationalId();

        VerifyResponse response = verificationService.verifyByCode(
                signatureCode, verifierIp, verifierNationalId);

        String message = response.isValid()
                ? "Signature record is valid"
                : "Signature lookup result: " + response.getMessage();

        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    /**
     * Resolves the verifier's nationalId from the Security context if they are
     * an authenticated citizen. Returns null for unauthenticated or admin callers.
     */
    private String resolveVerifierNationalId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof String nationalId) {
            // Citizen JWT — principal is nationalId
            return nationalId;
        }
        if (principal instanceof UserDetailsImpl) {
            // Admin — don't expose admin username as verifier nationalId
            return null;
        }
        // Anonymous / not authenticated
        return null;
    }
}
