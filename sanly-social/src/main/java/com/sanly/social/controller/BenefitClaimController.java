package com.sanly.social.controller;

import com.sanly.social.dto.request.ApproveClaimRequest;
import com.sanly.social.dto.request.AutoTriggerRequest;
import com.sanly.social.dto.request.RejectClaimRequest;
import com.sanly.social.dto.request.SubmitClaimRequest;
import com.sanly.social.dto.response.ClaimResponse;
import com.sanly.social.service.BenefitClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/social/claims")
@RequiredArgsConstructor
public class BenefitClaimController {

    private final BenefitClaimService claimService;

    @PostMapping
    public ResponseEntity<ClaimResponse> submit(@RequestBody SubmitClaimRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.submit(req));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ClaimResponse>> pending() {
        return ResponseEntity.ok(claimService.findPending());
    }

    @GetMapping("/{claimCode}")
    public ResponseEntity<ClaimResponse> findByCode(@PathVariable String claimCode) {
        return ResponseEntity.ok(claimService.findByCode(claimCode));
    }

    @GetMapping("/citizen/{nationalId}")
    public ResponseEntity<List<ClaimResponse>> findByCitizen(@PathVariable String nationalId) {
        return ResponseEntity.ok(claimService.findByCitizen(nationalId));
    }

    @PostMapping("/{claimCode}/approve")
    public ResponseEntity<ClaimResponse> approve(@PathVariable String claimCode,
                                                   @RequestBody(required = false) ApproveClaimRequest req) {
        return ResponseEntity.ok(claimService.approve(claimCode, req));
    }

    @PostMapping("/{claimCode}/reject")
    public ResponseEntity<ClaimResponse> reject(@PathVariable String claimCode,
                                                  @RequestBody RejectClaimRequest req) {
        return ResponseEntity.ok(claimService.reject(claimCode, req));
    }

    @PostMapping("/{claimCode}/suspend")
    public ResponseEntity<ClaimResponse> suspend(@PathVariable String claimCode) {
        return ResponseEntity.ok(claimService.suspend(claimCode));
    }

    @PostMapping("/{claimCode}/reinstate")
    public ResponseEntity<ClaimResponse> reinstate(@PathVariable String claimCode) {
        return ResponseEntity.ok(claimService.reinstate(claimCode));
    }

    // Life-event key protected — guarded by LifeEventKeyFilter + SecurityConfig
    @PostMapping("/auto-trigger")
    public ResponseEntity<ClaimResponse> autoTrigger(@RequestBody AutoTriggerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.autoTrigger(req));
    }

    // Life-event key protected
    @PostMapping("/cancel-all")
    public ResponseEntity<Map<String, Integer>> cancelAll(@RequestBody Map<String, String> body) {
        int count = claimService.cancelAll(body.get("citizenNationalId"));
        return ResponseEntity.ok(Map.of("cancelledCount", count));
    }
}
