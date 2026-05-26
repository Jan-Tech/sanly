package com.sanly.pension.controller;

import com.sanly.pension.dto.request.DeceasedAccountRequest;
import com.sanly.pension.dto.request.OpenAccountRequest;
import com.sanly.pension.dto.response.EligibilityResponse;
import com.sanly.pension.dto.response.PensionAccountResponse;
import com.sanly.pension.entity.AccountStatus;
import com.sanly.pension.service.PensionAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/pension/accounts")
@RequiredArgsConstructor
public class PensionAccountController {
    private final PensionAccountService accountService;

    @Value("${sanly.life-event.service-key:change-me-life-event-key}")
    private String expectedServiceKey;

    @PostMapping
    public ResponseEntity<PensionAccountResponse> open(@Valid @RequestBody OpenAccountRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.open(req));
    }

    @GetMapping("/{accountCode}")
    public ResponseEntity<PensionAccountResponse> findByCode(@PathVariable String accountCode) {
        return ResponseEntity.ok(accountService.findByCode(accountCode));
    }

    @GetMapping("/citizen/{nationalId}")
    public ResponseEntity<PensionAccountResponse> findByNin(@PathVariable String nationalId) {
        return ResponseEntity.ok(accountService.findByNin(nationalId));
    }

    @GetMapping("/citizen/{nationalId}/eligibility")
    public ResponseEntity<EligibilityResponse> getEligibility(@PathVariable String nationalId) {
        return ResponseEntity.ok(accountService.getEligibility(nationalId));
    }

    @PatchMapping("/{accountCode}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PensionAccountResponse> updateStatus(@PathVariable String accountCode,
                                                                @RequestParam AccountStatus status) {
        return ResponseEntity.ok(accountService.updateStatus(accountCode, status));
    }

    @PostMapping("/deceased")
    public ResponseEntity<Void> handleDeceased(
            @RequestHeader("X-Life-Event-Key") String serviceKey,
            @Valid @RequestBody DeceasedAccountRequest req) {
        if (!expectedServiceKey.equals(serviceKey)) {
            log.warn("Rejected deceased account call — invalid service key");
            return ResponseEntity.status(403).build();
        }
        accountService.handleDeceased(req.getDeceasedNationalId());
        return ResponseEntity.ok().build();
    }
}
