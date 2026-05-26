package com.sanly.pharmacy.controller;

import com.sanly.pharmacy.config.UserDetailsImpl;
import com.sanly.pharmacy.dto.request.DispenseRequest;
import com.sanly.pharmacy.dto.response.DispenseRecordResponse;
import com.sanly.pharmacy.service.DispenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dispense")
@PreAuthorize("hasAnyRole('PHARMACIST', 'MANAGER')")
@RequiredArgsConstructor
public class DispenseController {

    private final DispenseService dispenseService;

    @PostMapping
    public ResponseEntity<DispenseRecordResponse> dispense(
            @Valid @RequestBody DispenseRequest req,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(dispenseService.dispense(req, principal));
    }

    @GetMapping("/history")
    public ResponseEntity<List<DispenseRecordResponse>> getHistory() {
        return ResponseEntity.ok(dispenseService.getHistory());
    }

    @GetMapping("/history/citizen/{nationalId}")
    public ResponseEntity<List<DispenseRecordResponse>> getHistoryByCitizen(@PathVariable String nationalId) {
        return ResponseEntity.ok(dispenseService.getHistoryByCitizen(nationalId));
    }
}
