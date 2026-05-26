package com.sanly.signature.controller;

import com.sanly.signature.config.UserDetailsImpl;
import com.sanly.signature.dto.request.RevokeRequest;
import com.sanly.signature.dto.response.ApiResponse;
import com.sanly.signature.dto.response.PageResponse;
import com.sanly.signature.dto.response.SignatureResponse;
import com.sanly.signature.service.QrCodeService;
import com.sanly.signature.service.SignatureService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/signature")
public class SignatureController {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private final SignatureService signatureService;
    private final QrCodeService qrCodeService;

    public SignatureController(SignatureService signatureService,
                               QrCodeService qrCodeService) {
        this.signatureService = signatureService;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Signs a document. Only citizens (ROLE_CITIZEN) from citizen-registry JWTs may call this.
     * The citizen's nationalId is extracted from the JWT principal (set as String by JwtAuthenticationFilter).
     */
    @PostMapping("/sign")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<SignatureResponse>> sign(
            @RequestParam("file") MultipartFile file,
            @RequestParam("purpose") String purpose,
            @RequestParam("otpCode") String otpCode,
            @RequestParam(value = "language", defaultValue = "tk") String language,
            HttpServletRequest httpRequest) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document file must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Document exceeds maximum size of 10MB");
        }

        String nationalId = extractNationalId();
        String ipAddress = httpRequest.getRemoteAddr();

        SignatureResponse response = signatureService.sign(
                file, purpose, otpCode, nationalId, ipAddress, language);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Document signed successfully", response));
    }

    /**
     * Lists the authenticated citizen's own signatures (paginated).
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<PageResponse<SignatureResponse>>> getMySignatures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String nationalId = extractNationalId();
        PageResponse<SignatureResponse> result = signatureService.getMySignatures(nationalId, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Signatures retrieved", result));
    }

    /**
     * Gets a single signature by code. Citizens may only view their own; admins may view any.
     */
    @GetMapping("/{signatureCode}")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<SignatureResponse>> getByCode(
            @PathVariable String signatureCode) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String nationalId = isAdmin ? null : extractNationalId();

        SignatureResponse response = signatureService.getByCode(signatureCode, nationalId, isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Signature retrieved", response));
    }

    /**
     * Revokes a signature. Citizens may only revoke their own; admins may revoke any.
     */
    @PatchMapping("/{signatureCode}/revoke")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<SignatureResponse>> revoke(
            @PathVariable String signatureCode,
            @Valid @RequestBody RevokeRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String nationalId = isAdmin ? extractPrincipalOrNull() : extractNationalId();

        SignatureResponse response = signatureService.revoke(
                signatureCode, request.getReason(), nationalId, isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Signature revoked", response));
    }

    /**
     * Returns a QR code PNG for the given signature code.
     * The QR encodes https://sanly.tm/verify/{signatureCode} for public scanning.
     */
    @GetMapping(value = "/{signatureCode}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String signatureCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String nationalId = isAdmin ? null : extractNationalId();

        // Verify existence and access rights
        signatureService.getByCode(signatureCode, nationalId, isAdmin);

        byte[] qr = qrCodeService.generateQrCode(signatureCode);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qr);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Extracts the nationalId from the Security context principal.
     * Citizen JWTs set the principal as a String (nationalId).
     * Admin JWTs set it as UserDetailsImpl — no nationalId field expected here.
     */
    private String extractNationalId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof String nationalId) {
            return nationalId;
        }
        if (principal instanceof UserDetailsImpl) {
            // Admin calling a citizen-only endpoint — should not happen with @PreAuthorize, but guard anyway
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This endpoint requires a citizen token");
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cannot determine identity");
    }

    private String extractPrincipalOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof String s) return s;
        if (principal instanceof UserDetailsImpl u) return u.getUsername();
        return null;
    }
}
