package com.sanly.banking.controller;

import com.sanly.banking.dto.response.ApiResponse;
import com.sanly.banking.dto.response.CitizenProfileResponse;
import com.sanly.banking.service.DataAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/banking/citizen")
@RequiredArgsConstructor
@Tag(name = "Data Access", description = "Bank endpoint to fetch citizen profile data using a consent token")
public class DataAccessController {

    private final DataAccessService dataAccessService;

    @GetMapping("/profile")
    @Operation(
        summary = "Fetch citizen profile",
        description = "Bank uses a one-time consent token to fetch citizen data. " +
                      "IMPORTANT: The token is consumed immediately on this call — even if data fetch partially fails. " +
                      "Authenticated via X-Bank-Code + X-Bank-Key + X-Consent-Token headers."
    )
    public ResponseEntity<ApiResponse<CitizenProfileResponse>> fetchCitizenProfile(
            @RequestHeader("X-Bank-Code")      String bankCode,
            @RequestHeader("X-Bank-Key")       String bankKey,
            @RequestHeader("X-Consent-Token")  String consentToken,
            HttpServletRequest request) {

        String ipAddress = extractIp(request);
        CitizenProfileResponse profile = dataAccessService.fetchCitizenProfile(
                bankCode, bankKey, consentToken, ipAddress);

        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
