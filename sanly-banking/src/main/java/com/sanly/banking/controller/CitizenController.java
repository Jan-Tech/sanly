package com.sanly.banking.controller;

import com.sanly.banking.dto.response.ApiResponse;
import com.sanly.banking.entity.BankDataAccess;
import com.sanly.banking.service.DataAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/banking/my")
@RequiredArgsConstructor
@Tag(name = "Citizen Portal", description = "Citizen self-service endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CitizenController {

    private final DataAccessService dataAccessService;

    @GetMapping("/access-history")
    @Operation(summary = "Get my data access history",
               description = "Returns a paginated list of all times banks have accessed data using my consent.")
    public ResponseEntity<ApiResponse<Page<BankDataAccess>>> getMyAccessHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {

        String citizenNationalId = extractNationalId(auth);
        Page<BankDataAccess> history = dataAccessService.getAccessHistoryForCitizen(
                citizenNationalId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    private String extractNationalId(Authentication auth) {
        if (auth.getPrincipal() instanceof String) {
            return (String) auth.getPrincipal();
        }
        throw new IllegalStateException("Citizen authentication required");
    }
}
