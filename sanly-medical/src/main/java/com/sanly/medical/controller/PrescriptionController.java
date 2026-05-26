package com.sanly.medical.controller;

import com.sanly.medical.config.UserDetailsImpl;
import com.sanly.medical.dto.request.DispensePrescriptionRequest;
import com.sanly.medical.dto.request.IssuePrescriptionRequest;
import com.sanly.medical.dto.response.DispensingResponse;
import com.sanly.medical.dto.response.PrescriptionResponse;
import com.sanly.medical.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /** Issue a new prescription. Doctor must be authenticated; clinic scoped automatically. */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> issue(
            @Valid @RequestBody IssuePrescriptionRequest req,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.issue(req, principal.getDoctorId(), principal.getClinicId()));
    }

    @GetMapping("/{prescriptionCode}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<PrescriptionResponse> getByCode(@PathVariable String prescriptionCode) {
        return ResponseEntity.ok(prescriptionService.getByCode(prescriptionCode));
    }

    @GetMapping("/citizen/{nationalId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<PrescriptionResponse>> getByCitizen(@PathVariable String nationalId) {
        return ResponseEntity.ok(prescriptionService.getByCitizen(nationalId));
    }

    @PatchMapping("/{prescriptionCode}/cancel")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> cancel(
            @PathVariable String prescriptionCode,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(prescriptionService.cancel(prescriptionCode, principal.getDoctorId()));
    }

    /**
     * Dispense endpoint — authenticated by PharmacyKeyAuthFilter via X-Pharmacy-Code + X-Pharmacy-Key.
     * The pharmacy code is taken from the Security context principal set by the filter.
     */
    @PostMapping("/{prescriptionCode}/dispense")
    @PreAuthorize("hasRole('PHARMACY')")
    public ResponseEntity<DispensingResponse> dispense(
            @PathVariable String prescriptionCode,
            @Valid @RequestBody DispensePrescriptionRequest req,
            @AuthenticationPrincipal String pharmacyCode) {
        return ResponseEntity.ok(prescriptionService.dispense(prescriptionCode, pharmacyCode, req));
    }
}
