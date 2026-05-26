package com.sanly.medical.controller;

import com.sanly.medical.dto.request.PharmacyStatusRequest;
import com.sanly.medical.dto.request.RegisterPharmacyRequest;
import com.sanly.medical.dto.response.PharmacyResponse;
import com.sanly.medical.service.PharmacyManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacies")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyManagementService pharmacyService;

    @PostMapping
    public ResponseEntity<PharmacyResponse> register(@Valid @RequestBody RegisterPharmacyRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pharmacyService.register(req));
    }

    @GetMapping
    public ResponseEntity<List<PharmacyResponse>> listAll() {
        return ResponseEntity.ok(pharmacyService.listAll());
    }

    @PatchMapping("/{pharmacyCode}/status")
    public ResponseEntity<PharmacyResponse> updateStatus(
            @PathVariable String pharmacyCode,
            @Valid @RequestBody PharmacyStatusRequest req) {
        return ResponseEntity.ok(pharmacyService.updateStatus(pharmacyCode, req));
    }

    @PostMapping("/{pharmacyCode}/rotate-key")
    public ResponseEntity<PharmacyResponse> rotateKey(@PathVariable String pharmacyCode) {
        return ResponseEntity.ok(pharmacyService.rotateKey(pharmacyCode));
    }
}
