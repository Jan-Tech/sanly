package com.sanly.documents.controller;

import com.sanly.documents.config.UserDetailsImpl;
import com.sanly.documents.dto.request.GenerateCertificateRequest;
import com.sanly.documents.dto.response.ApiResponse;
import com.sanly.documents.dto.response.CertificateResponse;
import com.sanly.documents.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    /**
     * Generate a certified PDF. The JWT token of the citizen is forwarded to source services.
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<byte[]> generate(
            @Valid @RequestBody GenerateCertificateRequest req,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String nationalId = getCitizenNationalId();
        String citizenToken = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : null;

        byte[] pdfBytes = certificateService.generate(
                req.getDocumentType(),
                req.getSourceRecordCode(),
                nationalId,
                citizenToken);

        // We don't know the certCode here without an extra lookup, use a generic name
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate.pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    /**
     * List all certificates belonging to the authenticated citizen.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getMyList() {
        String nationalId = getCitizenNationalId();
        return ResponseEntity.ok(ApiResponse.ok(certificateService.getMyList(nationalId)));
    }

    /**
     * Get certificate metadata by code.
     */
    @GetMapping("/{certCode}")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<CertificateResponse>> getByCode(@PathVariable String certCode) {
        boolean isAdmin = isAdmin();
        String nationalId = isAdmin ? null : getCitizenNationalId();
        return ResponseEntity.ok(ApiResponse.ok(
                certificateService.getByCode(certCode, nationalId, isAdmin)));
    }

    /**
     * Download certificate PDF by code.
     */
    @GetMapping("/{certCode}/download")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<byte[]> download(@PathVariable String certCode) {
        boolean isAdmin = isAdmin();
        String nationalId = isAdmin ? null : getCitizenNationalId();

        byte[] pdfBytes = certificateService.download(certCode, nationalId, isAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "cert-" + certCode + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    /**
     * Revoke (self-revoke or admin revoke) a certificate.
     */
    @DeleteMapping("/{certCode}")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revoke(
            @PathVariable String certCode,
            @RequestParam(defaultValue = "Revoked by owner") String reason) {
        boolean isAdmin = isAdmin();
        String nationalId = isAdmin ? null : getCitizenNationalId();
        certificateService.revoke(certCode, nationalId, reason, isAdmin);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Certificate revoked successfully")
                .build());
    }

    // ---- Helpers ----

    private String getCitizenNationalId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        if (principal instanceof UserDetailsImpl u) {
            return u.getUsername();
        }
        throw new IllegalStateException("Could not extract national ID from security context");
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
