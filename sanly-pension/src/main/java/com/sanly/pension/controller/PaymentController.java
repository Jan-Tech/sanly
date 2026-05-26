package com.sanly.pension.controller;

import com.sanly.pension.dto.response.PensionPaymentResponse;
import com.sanly.pension.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pension/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/account/{accountCode}")
    public ResponseEntity<List<PensionPaymentResponse>> findByAccount(@PathVariable String accountCode) {
        return ResponseEntity.ok(paymentService.findByAccount(accountCode));
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<PensionPaymentResponse>> findScheduled() {
        return ResponseEntity.ok(paymentService.findScheduledThisMonth());
    }

    @PatchMapping("/{paymentId}/mark-paid")
    public ResponseEntity<PensionPaymentResponse> markPaid(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.markPaid(paymentId));
    }
}
