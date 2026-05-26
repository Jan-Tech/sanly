package com.sanly.banking.controller;

import com.sanly.banking.config.OfficerUserDetails;
import com.sanly.banking.dto.request.RegisterBankRequest;
import com.sanly.banking.dto.request.UpdateBankStatusRequest;
import com.sanly.banking.dto.response.ApiResponse;
import com.sanly.banking.dto.response.RegisterBankResponse;
import com.sanly.banking.dto.response.RegisteredBankResponse;
import com.sanly.banking.service.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/banking/banks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Bank Management", description = "Admin endpoints for managing registered banks")
@SecurityRequirement(name = "bearerAuth")
public class BankController {

    private final BankService bankService;

    @PostMapping
    @Operation(summary = "Register a new bank", description = "Creates a new bank registration with a one-time API key")
    public ResponseEntity<ApiResponse<RegisterBankResponse>> registerBank(
            @Valid @RequestBody RegisterBankRequest request,
            @AuthenticationPrincipal OfficerUserDetails officer) {

        UUID officerId = officer != null ? officer.getOfficer().getOfficerId() : null;
        RegisterBankResponse response = bankService.registerBank(request, officerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Bank registered successfully. Store the API key securely — it will not be shown again.", response));
    }

    @GetMapping
    @Operation(summary = "List all banks")
    public ResponseEntity<ApiResponse<List<RegisteredBankResponse>>> getAllBanks() {
        return ResponseEntity.ok(ApiResponse.ok(bankService.getAllBanks()));
    }

    @GetMapping("/{bankCode}")
    @Operation(summary = "Get bank details")
    public ResponseEntity<ApiResponse<RegisteredBankResponse>> getBank(@PathVariable String bankCode) {
        return ResponseEntity.ok(ApiResponse.ok(bankService.getBankByCode(bankCode)));
    }

    @PatchMapping("/{bankCode}/approve")
    @Operation(summary = "Approve a pending bank")
    public ResponseEntity<ApiResponse<RegisteredBankResponse>> approveBank(
            @PathVariable String bankCode,
            @AuthenticationPrincipal OfficerUserDetails officer) {

        UUID officerId = officer != null ? officer.getOfficer().getOfficerId() : null;
        return ResponseEntity.ok(ApiResponse.ok("Bank approved", bankService.approveBank(bankCode, officerId)));
    }

    @PatchMapping("/{bankCode}/status")
    @Operation(summary = "Update bank status (ACTIVE/SUSPENDED/REVOKED)")
    public ResponseEntity<ApiResponse<RegisteredBankResponse>> updateStatus(
            @PathVariable String bankCode,
            @Valid @RequestBody UpdateBankStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Bank status updated",
                bankService.updateBankStatus(bankCode, request.status())));
    }

    @PostMapping("/{bankCode}/rotate-key")
    @Operation(summary = "Rotate bank API key",
               description = "Generates a new API key. Store it securely — it will not be shown again.")
    public ResponseEntity<ApiResponse<Map<String, String>>> rotateKey(@PathVariable String bankCode) {
        String newKey = bankService.rotateApiKey(bankCode);
        return ResponseEntity.ok(ApiResponse.ok("API key rotated successfully",
                Map.of("bankCode", bankCode, "newApiKey", newKey)));
    }
}
