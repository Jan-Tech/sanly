package com.sanly.documents.controller;

import com.sanly.documents.dto.response.*;
import com.sanly.documents.service.CertificateService;
import com.sanly.documents.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final CertificateService certificateService;
    private final TrackingService trackingService;

    /**
     * Paginated list of all certificates.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<CertificateResponse>>> getAllCertificates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(certificateService.adminGetAll(page, size)));
    }

    /**
     * Admin revoke a certificate.
     */
    @PatchMapping("/{certCode}/revoke")
    public ResponseEntity<ApiResponse<CertificateResponse>> revoke(
            @PathVariable String certCode,
            @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.ok(certificateService.adminRevoke(certCode, reason)));
    }

    /**
     * Service-wide statistics for the admin dashboard.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DocStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(certificateService.getStats()));
    }

    /**
     * Paginated list of all tracked items.
     */
    @GetMapping("/tracking")
    public ResponseEntity<ApiResponse<PageResponse<TrackedItemResponse>>> getAllTrackedItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(trackingService.adminGetAll(page, size)));
    }
}
