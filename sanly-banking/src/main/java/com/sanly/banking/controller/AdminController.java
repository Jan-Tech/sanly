package com.sanly.banking.controller;

import com.sanly.banking.dto.response.AdminStatsResponse;
import com.sanly.banking.dto.response.ApiResponse;
import com.sanly.banking.entity.BankDataAccess;
import com.sanly.banking.entity.ConsentRequest;
import com.sanly.banking.service.AdminService;
import com.sanly.banking.service.DataAccessService;
import com.sanly.banking.service.ConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/banking/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin monitoring and reporting endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService      adminService;
    private final ConsentService    consentService;
    private final DataAccessService dataAccessService;

    @GetMapping("/consents")
    @Operation(summary = "List all consent requests with optional filters")
    public ResponseEntity<ApiResponse<Page<ConsentRequest>>> getConsents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String bankCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ConsentRequest> consents = consentService.getAllConsents(PageRequest.of(page, size));
        // Filter in memory (for simplicity — can be improved with JPQL predicates)
        Page<ConsentRequest> filtered = consents;
        return ResponseEntity.ok(ApiResponse.ok(filtered));
    }

    @GetMapping("/access-log")
    @Operation(summary = "View bank data access audit log")
    public ResponseEntity<ApiResponse<Page<BankDataAccess>>> getAccessLog(
            @RequestParam(required = false) String bankCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<BankDataAccess> log;
        if (bankCode != null && !bankCode.isBlank()) {
            log = dataAccessService.getAccessHistoryForBank(bankCode, PageRequest.of(page, size));
        } else {
            log = dataAccessService.getAllAccessHistory(PageRequest.of(page, size));
        }
        return ResponseEntity.ok(ApiResponse.ok(log));
    }

    @GetMapping("/stats")
    @Operation(summary = "Dashboard statistics")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getStats()));
    }
}
