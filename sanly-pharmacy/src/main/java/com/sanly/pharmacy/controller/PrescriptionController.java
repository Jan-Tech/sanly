package com.sanly.pharmacy.controller;

import com.sanly.pharmacy.dto.response.PrescriptionLookupResponse;
import com.sanly.pharmacy.service.PrescriptionLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
@PreAuthorize("hasAnyRole('PHARMACIST', 'MANAGER')")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionLookupService lookupService;

    /**
     * Look up a prescription by code. Requires the citizen's NIN to query the bridge.
     * In practice the citizen provides their NIN at the counter along with the prescription code.
     */
    @GetMapping("/{prescriptionCode}")
    public ResponseEntity<PrescriptionLookupResponse> getByCode(
            @PathVariable String prescriptionCode,
            @RequestParam String nationalId) {
        PrescriptionLookupResponse resp = lookupService.getByCode(prescriptionCode, nationalId);
        return resp != null ? ResponseEntity.ok(resp) : ResponseEntity.notFound().build();
    }

    /** Get all active prescriptions for a citizen by NIN. */
    @GetMapping("/citizen/{nationalId}")
    public ResponseEntity<List<PrescriptionLookupResponse>> getByCitizen(@PathVariable String nationalId) {
        return ResponseEntity.ok(lookupService.getActiveByCitizen(nationalId));
    }
}
