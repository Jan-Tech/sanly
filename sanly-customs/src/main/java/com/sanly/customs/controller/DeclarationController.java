package com.sanly.customs.controller;

import com.sanly.customs.dto.request.CreateDeclarationRequest;
import com.sanly.customs.dto.request.RecordPaymentRequest;
import com.sanly.customs.dto.request.RejectDeclarationRequest;
import com.sanly.customs.dto.response.DeclarationResponse;
import com.sanly.customs.entity.DeclarationStatus;
import com.sanly.customs.service.DeclarationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customs/declarations")
@RequiredArgsConstructor
public class DeclarationController {
    private final DeclarationService declarationService;

    @PostMapping
    public ResponseEntity<DeclarationResponse> create(@RequestBody CreateDeclarationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(declarationService.create(req));
    }

    @GetMapping("/{declarationCode}")
    public ResponseEntity<DeclarationResponse> findByCode(@PathVariable String declarationCode) {
        return ResponseEntity.ok(declarationService.findByCode(declarationCode));
    }

    // Public verify — no auth (permitted in SecurityConfig)
    @GetMapping("/verify/{declarationCode}")
    public ResponseEntity<DeclarationResponse> verify(@PathVariable String declarationCode) {
        return ResponseEntity.ok(declarationService.verify(declarationCode));
    }

    @GetMapping("/declarant/{id}")
    public ResponseEntity<List<DeclarationResponse>> byDeclarant(@PathVariable String id) {
        return ResponseEntity.ok(declarationService.findByDeclarant(id));
    }

    @GetMapping("/port/{portCode}")
    public ResponseEntity<List<DeclarationResponse>> byPort(
            @PathVariable String portCode,
            @RequestParam(required = false) DeclarationStatus status) {
        return ResponseEntity.ok(declarationService.findByPort(portCode, status));
    }

    @PatchMapping("/{declarationCode}/submit")
    public ResponseEntity<DeclarationResponse> submit(@PathVariable String declarationCode) {
        return ResponseEntity.ok(declarationService.submit(declarationCode));
    }

    @PatchMapping("/{declarationCode}/clear")
    public ResponseEntity<DeclarationResponse> clear(@PathVariable String declarationCode) {
        return ResponseEntity.ok(declarationService.clear(declarationCode));
    }

    @PatchMapping("/{declarationCode}/reject")
    public ResponseEntity<DeclarationResponse> reject(@PathVariable String declarationCode,
                                                       @RequestBody RejectDeclarationRequest req) {
        return ResponseEntity.ok(declarationService.reject(declarationCode, req));
    }

    @PatchMapping("/{declarationCode}/hold")
    public ResponseEntity<DeclarationResponse> hold(@PathVariable String declarationCode) {
        return ResponseEntity.ok(declarationService.hold(declarationCode));
    }

    @PatchMapping("/{declarationCode}/record-payment")
    public ResponseEntity<DeclarationResponse> recordPayment(@PathVariable String declarationCode,
                                                              @RequestBody RecordPaymentRequest req) {
        return ResponseEntity.ok(declarationService.recordPayment(declarationCode, req));
    }
}
