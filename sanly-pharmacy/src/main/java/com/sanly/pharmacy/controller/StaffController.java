package com.sanly.pharmacy.controller;

import com.sanly.pharmacy.config.UserDetailsImpl;
import com.sanly.pharmacy.dto.request.CreateStaffRequest;
import com.sanly.pharmacy.dto.request.StaffStatusRequest;
import com.sanly.pharmacy.dto.response.StaffResponse;
import com.sanly.pharmacy.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    public ResponseEntity<StaffResponse> create(
            @Valid @RequestBody CreateStaffRequest req,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.create(req, principal));
    }

    @GetMapping
    public ResponseEntity<List<StaffResponse>> listAll() {
        return ResponseEntity.ok(staffService.listAll());
    }

    @PatchMapping("/{staffId}/status")
    public ResponseEntity<StaffResponse> updateStatus(
            @PathVariable Long staffId,
            @Valid @RequestBody StaffStatusRequest req) {
        return ResponseEntity.ok(staffService.updateStatus(staffId, req));
    }
}
