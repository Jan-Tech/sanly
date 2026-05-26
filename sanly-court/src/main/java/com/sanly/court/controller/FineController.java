package com.sanly.court.controller;

import com.sanly.court.dto.request.CitizenPayRequest;
import com.sanly.court.dto.request.IssueFineRequest;
import com.sanly.court.dto.response.FineResponse;
import com.sanly.court.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/court/fines")
@RequiredArgsConstructor
public class FineController {
    private final FineService fineService;

    @PostMapping
    public ResponseEntity<FineResponse> issue(@RequestBody IssueFineRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fineService.issue(req));
    }

    @GetMapping("/{fineCode}")
    public ResponseEntity<FineResponse> findByCode(@PathVariable String fineCode) {
        return ResponseEntity.ok(fineService.findByCode(fineCode));
    }

    @GetMapping("/citizen/{nationalId}")
    public ResponseEntity<List<FineResponse>> findByCitizen(@PathVariable String nationalId) {
        return ResponseEntity.ok(fineService.findByCitizen(nationalId));
    }

    // Public verify — permitted in SecurityConfig
    @GetMapping("/verify/{fineCode}")
    public ResponseEntity<FineResponse> verify(@PathVariable String fineCode) {
        return ResponseEntity.ok(fineService.verify(fineCode));
    }

    @PatchMapping("/{fineCode}/pay")
    public ResponseEntity<FineResponse> markPaid(@PathVariable String fineCode) {
        return ResponseEntity.ok(fineService.markPaid(fineCode));
    }

    // Citizen self-reports payment — any authenticated user; service validates fine belongs to citizen
    @PatchMapping("/{fineCode}/citizen-pay")
    public ResponseEntity<FineResponse> citizenPay(@PathVariable String fineCode,
                                                     @RequestBody CitizenPayRequest req) {
        return ResponseEntity.ok(fineService.citizenPay(fineCode, req));
    }

    @PatchMapping("/{fineCode}/verify-payment")
    public ResponseEntity<FineResponse> verifyPayment(@PathVariable String fineCode) {
        return ResponseEntity.ok(fineService.verifyPayment(fineCode));
    }

    @PatchMapping("/{fineCode}/waive")
    @PreAuthorize("hasRole('JUDGE')")
    public ResponseEntity<FineResponse> waive(@PathVariable String fineCode,
                                               @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(fineService.waive(fineCode, body.get("reason")));
    }
}
