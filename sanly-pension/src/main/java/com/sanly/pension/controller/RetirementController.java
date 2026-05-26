package com.sanly.pension.controller;

import com.sanly.pension.dto.request.ApplyRetirementRequest;
import com.sanly.pension.dto.request.RejectRetirementRequest;
import com.sanly.pension.dto.response.RetirementApplicationResponse;
import com.sanly.pension.entity.ApplicationStatus;
import com.sanly.pension.service.RetirementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pension/retirement")
@RequiredArgsConstructor
public class RetirementController {
    private final RetirementService retirementService;

    @PostMapping("/apply")
    public ResponseEntity<RetirementApplicationResponse> apply(@Valid @RequestBody ApplyRetirementRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(retirementService.apply(req));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<RetirementApplicationResponse> findById(@PathVariable Long applicationId) {
        return ResponseEntity.ok(retirementService.findById(applicationId));
    }

    @GetMapping("/citizen/{nationalId}")
    public ResponseEntity<List<RetirementApplicationResponse>> findByNin(@PathVariable String nationalId) {
        return ResponseEntity.ok(retirementService.findByNin(nationalId));
    }

    @GetMapping
    public ResponseEntity<List<RetirementApplicationResponse>> findByStatus(
            @RequestParam(defaultValue = "PENDING") ApplicationStatus status) {
        return ResponseEntity.ok(retirementService.findByStatus(status));
    }

    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<RetirementApplicationResponse> approve(@PathVariable Long applicationId) {
        return ResponseEntity.ok(retirementService.approve(applicationId));
    }

    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<RetirementApplicationResponse> reject(@PathVariable Long applicationId,
                                                                 @Valid @RequestBody RejectRetirementRequest req) {
        return ResponseEntity.ok(retirementService.reject(applicationId, req));
    }
}
