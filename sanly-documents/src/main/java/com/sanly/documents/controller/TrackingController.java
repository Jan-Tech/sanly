package com.sanly.documents.controller;

import com.sanly.documents.dto.request.TrackingUpdateRequest;
import com.sanly.documents.dto.response.ApiResponse;
import com.sanly.documents.dto.response.TrackedItemResponse;
import com.sanly.documents.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @Value("${tracking.service-key:tracking-internal-key-change-me}")
    private String trackingServiceKey;

    /**
     * Internal endpoint — called by other services to push tracking updates.
     * Authenticated via X-Service-Key header (not citizen/admin JWT).
     * This endpoint is permitAll in SecurityConfig — key validation done here.
     */
    @PostMapping("/api/v1/documents/tracking/update")
    public ResponseEntity<ApiResponse<TrackedItemResponse>> pushUpdate(
            @RequestHeader("X-Service-Key") String serviceKey,
            @Valid @RequestBody TrackingUpdateRequest req) {

        if (!trackingServiceKey.equals(serviceKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service key");
        }

        TrackedItemResponse result = trackingService.pushUpdate(req);
        return ResponseEntity.ok(ApiResponse.ok("Tracking updated", result));
    }

    /**
     * Returns all tracking items for the authenticated citizen.
     * Optional filter by completion status.
     */
    @GetMapping("/api/v1/documents/tracking/my")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<List<TrackedItemResponse>>> getMyItems(
            @RequestParam(required = false) Boolean isCompleted) {
        String nationalId = getCitizenNationalId();
        return ResponseEntity.ok(ApiResponse.ok(trackingService.getMyItems(nationalId, isCompleted)));
    }

    /**
     * Returns a specific tracking item with full update timeline.
     */
    @GetMapping("/api/v1/documents/tracking/{trackingCode}")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<TrackedItemResponse>> getByCode(
            @PathVariable String trackingCode) {
        String nationalId = getCitizenNationalId();
        return ResponseEntity.ok(ApiResponse.ok(trackingService.getByCode(trackingCode, nationalId)));
    }

    /**
     * Search for a tracking item by original source code.
     */
    @GetMapping("/api/v1/documents/tracking/search")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<TrackedItemResponse>> searchBySourceCode(
            @RequestParam String code) {
        String nationalId = getCitizenNationalId();
        return ResponseEntity.ok(ApiResponse.ok(trackingService.searchBySourceCode(code, nationalId)));
    }

    // ---- Helpers ----

    private String getCitizenNationalId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String s) return s;
        throw new IllegalStateException("Could not extract national ID from security context");
    }
}
