package com.sanly.documents.controller;

import com.sanly.documents.dto.response.ApiResponse;
import com.sanly.documents.dto.response.VerifyResponse;
import com.sanly.documents.service.CertificateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents/verify")
@RequiredArgsConstructor
public class VerifyController {

    private final CertificateService certificateService;

    /**
     * Verify a certificate by code (GET — for QR code landing pages and direct URL sharing).
     */
    @GetMapping("/{certCode}")
    public ResponseEntity<ApiResponse<VerifyResponse>> verifyByCode(
            @PathVariable String certCode,
            @RequestParam(required = false) String verifierNationalId,
            HttpServletRequest request) {

        String ip = extractIp(request);
        VerifyResponse result = certificateService.verifyByCode(certCode, ip, verifierNationalId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Verify a certificate by code (POST — for programmatic/form-based verification).
     * Accepts a certificateCode parameter. No PDF parsing — keep it simple.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VerifyResponse>> verifyPost(
            @RequestParam String certificateCode,
            @RequestParam(required = false) String verifierNationalId,
            HttpServletRequest request) {

        String ip = extractIp(request);
        VerifyResponse result = certificateService.verifyByCode(certificateCode, ip, verifierNationalId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ---- Helpers ----

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
