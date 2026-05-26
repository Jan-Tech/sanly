package com.sanly.dmv.controller;

import com.sanly.dmv.client.CitizenRegistryClient;
import com.sanly.dmv.dto.response.ApiResponse;
import com.sanly.dmv.dto.response.CitizenVerifyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/citizens")
@RequiredArgsConstructor
@Tag(name = "Citizen Lookup", description = "Verify citizen via SANLY Citizen Registry — before creating applications")
@SecurityRequirement(name = "bearerAuth")
public class CitizenLookupController {

    private final CitizenRegistryClient citizenClient;

    @GetMapping("/{nationalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    @Operation(summary = "Verify citizen by National ID — proxied to citizen-registry")
    public ApiResponse<CitizenVerifyResponse> verify(@PathVariable String nationalId) {
        return ApiResponse.<CitizenVerifyResponse>builder()
                .success(true)
                .data(citizenClient.verify(nationalId))
                .build();
    }
}
