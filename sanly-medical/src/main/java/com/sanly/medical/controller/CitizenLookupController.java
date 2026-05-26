package com.sanly.medical.controller;

import com.sanly.medical.client.CitizenRegistryClient;
import com.sanly.medical.dto.response.ApiResponse;
import com.sanly.medical.dto.response.CitizenVerifyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Proxies the citizen verify call to SANLY Citizen Registry.
 * Doctors use this to confirm a citizen exists and is ACTIVE before
 * creating a medical record.
 */
@RestController
@RequestMapping("/api/v1/citizens")
@RequiredArgsConstructor
@Tag(name = "Citizen Lookup", description = "Verify citizen identity via SANLY Citizen Registry")
@SecurityRequirement(name = "bearerAuth")
public class CitizenLookupController {

    private final CitizenRegistryClient citizenRegistryClient;

    @GetMapping("/{nationalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Verify a citizen by National ID — proxied to citizen-registry")
    public ApiResponse<CitizenVerifyResponse> verify(@PathVariable String nationalId) {
        CitizenVerifyResponse result = citizenRegistryClient.verify(nationalId);
        return ApiResponse.<CitizenVerifyResponse>builder()
                .success(true)
                .data(result)
                .build();
    }
}
