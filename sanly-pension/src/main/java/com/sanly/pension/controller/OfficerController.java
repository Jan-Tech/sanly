package com.sanly.pension.controller;

import com.sanly.pension.dto.request.CreateOfficerRequest;
import com.sanly.pension.dto.response.OfficerResponse;
import com.sanly.pension.entity.OfficerStatus;
import com.sanly.pension.service.OfficerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pension/officers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class OfficerController {
    private final OfficerService officerService;

    @PostMapping
    public ResponseEntity<OfficerResponse> create(@Valid @RequestBody CreateOfficerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(officerService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<OfficerResponse>> findAll() {
        return ResponseEntity.ok(officerService.findAll());
    }

    @PatchMapping("/{officerId}/status")
    public ResponseEntity<OfficerResponse> updateStatus(@PathVariable Long officerId,
                                                         @RequestParam OfficerStatus status) {
        return ResponseEntity.ok(officerService.updateStatus(officerId, status));
    }
}
