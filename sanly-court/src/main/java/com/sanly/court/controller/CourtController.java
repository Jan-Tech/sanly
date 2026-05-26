package com.sanly.court.controller;

import com.sanly.court.dto.request.CreateCourtRequest;
import com.sanly.court.dto.response.CourtResponse;
import com.sanly.court.entity.CourtStatus;
import com.sanly.court.service.CourtManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/court/courts")
@RequiredArgsConstructor
public class CourtController {
    private final CourtManagementService courtService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourtResponse> create(@RequestBody CreateCourtRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courtService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<CourtResponse>> findAll() {
        return ResponseEntity.ok(courtService.findAll());
    }

    @GetMapping("/{courtCode}")
    public ResponseEntity<CourtResponse> findByCode(@PathVariable String courtCode) {
        return ResponseEntity.ok(courtService.findByCode(courtCode));
    }

    @PatchMapping("/{courtCode}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourtResponse> updateStatus(@PathVariable String courtCode,
                                                       @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(courtService.updateStatus(courtCode, CourtStatus.valueOf(body.get("status"))));
    }
}
