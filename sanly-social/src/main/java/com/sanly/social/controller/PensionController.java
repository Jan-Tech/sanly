package com.sanly.social.controller;

import com.sanly.social.dto.request.ContributeRequest;
import com.sanly.social.dto.request.OpenPensionRequest;
import com.sanly.social.dto.response.PensionEligibilityResponse;
import com.sanly.social.dto.response.PensionResponse;
import com.sanly.social.service.PensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/social/pensions")
@RequiredArgsConstructor
public class PensionController {

    private final PensionService pensionService;

    @PostMapping
    public ResponseEntity<PensionResponse> open(@RequestBody OpenPensionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pensionService.open(req));
    }

    @GetMapping("/{nationalId}")
    public ResponseEntity<PensionResponse> getDetails(@PathVariable String nationalId) {
        return ResponseEntity.ok(pensionService.getDetails(nationalId));
    }

    @PatchMapping("/{nationalId}/contribute")
    public ResponseEntity<PensionResponse> contribute(@PathVariable String nationalId,
                                                        @RequestBody ContributeRequest req) {
        return ResponseEntity.ok(pensionService.contribute(nationalId, req));
    }

    @GetMapping("/{nationalId}/eligibility")
    public ResponseEntity<PensionEligibilityResponse> checkEligibility(@PathVariable String nationalId) {
        return ResponseEntity.ok(pensionService.checkEligibility(nationalId));
    }

    @PostMapping("/{nationalId}/start-paying")
    public ResponseEntity<PensionResponse> startPaying(@PathVariable String nationalId) {
        return ResponseEntity.ok(pensionService.startPaying(nationalId));
    }
}
