package com.sanly.customs.controller;

import com.sanly.customs.dto.request.CreateOfficerRequest;
import com.sanly.customs.dto.response.OfficerResponse;
import com.sanly.customs.entity.OfficerStatus;
import com.sanly.customs.service.OfficerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customs/officers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OfficerController {
    private final OfficerService officerService;

    @PostMapping
    public ResponseEntity<OfficerResponse> create(@RequestBody CreateOfficerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(officerService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<OfficerResponse>> findAll() {
        return ResponseEntity.ok(officerService.findAll());
    }

    @PatchMapping("/{officerId}/status")
    public ResponseEntity<OfficerResponse> updateStatus(@PathVariable UUID officerId,
                                                         @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(officerService.updateStatus(officerId, OfficerStatus.valueOf(body.get("status"))));
    }
}
