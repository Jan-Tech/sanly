package com.sanly.signature.controller;

import com.sanly.signature.dto.response.ApiResponse;
import com.sanly.signature.dto.response.PageResponse;
import com.sanly.signature.dto.response.SignatureResponse;
import com.sanly.signature.dto.response.SignatureStatsResponse;
import com.sanly.signature.service.SignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/signature/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final SignatureService signatureService;

    public AdminController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    /**
     * Returns all signature records, ordered by signed date descending, paginated.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<SignatureResponse>>> getAllSignatures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<SignatureResponse> result = signatureService.adminGetAll(page, size);
        return ResponseEntity.ok(ApiResponse.ok("All signatures retrieved", result));
    }

    /**
     * Returns platform-wide signing and verification statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SignatureStatsResponse>> getStats() {
        SignatureStatsResponse stats = signatureService.getStats();
        return ResponseEntity.ok(ApiResponse.ok("Statistics retrieved", stats));
    }
}
