package com.sanly.social.controller;

import com.sanly.social.dto.response.PaymentResponse;
import com.sanly.social.service.BenefitPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/payments")
@RequiredArgsConstructor
public class BenefitPaymentController {

    private final BenefitPaymentService paymentService;

    @GetMapping("/claim/{claimCode}")
    public ResponseEntity<List<PaymentResponse>> byClaimCode(@PathVariable String claimCode) {
        return ResponseEntity.ok(paymentService.findByClaimCode(claimCode));
    }

    @GetMapping("/citizen/{nationalId}")
    public ResponseEntity<List<PaymentResponse>> byCitizen(@PathVariable String nationalId) {
        return ResponseEntity.ok(paymentService.findByCitizen(nationalId));
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<PaymentResponse>> scheduledThisMonth() {
        return ResponseEntity.ok(paymentService.findScheduledThisMonth());
    }

    @PatchMapping("/{paymentId}/mark-paid")
    public ResponseEntity<PaymentResponse> markPaid(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.markPaid(paymentId));
    }
}
