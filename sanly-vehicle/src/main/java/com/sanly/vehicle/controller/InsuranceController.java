package com.sanly.vehicle.controller;

import com.sanly.vehicle.dto.request.RegisterInsuranceRequest;
import com.sanly.vehicle.dto.response.InsuranceResponse;
import com.sanly.vehicle.dto.response.InsuranceVerifyResponse;
import com.sanly.vehicle.service.InsuranceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicle/insurance")
@RequiredArgsConstructor
public class InsuranceController {
    private final InsuranceService insuranceService;

    @PostMapping
    public ResponseEntity<InsuranceResponse> register(@Valid @RequestBody RegisterInsuranceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(insuranceService.register(req));
    }

    @GetMapping("/vehicle/{plateNumber}")
    public ResponseEntity<List<InsuranceResponse>> findByPlate(@PathVariable String plateNumber) {
        return ResponseEntity.ok(insuranceService.findByPlate(plateNumber));
    }

    @GetMapping("/verify/{plateNumber}")
    public ResponseEntity<InsuranceVerifyResponse> verify(@PathVariable String plateNumber) {
        return ResponseEntity.ok(insuranceService.verify(plateNumber));
    }

    @PatchMapping("/{insuranceId}/cancel")
    public ResponseEntity<InsuranceResponse> cancel(@PathVariable Long insuranceId) {
        return ResponseEntity.ok(insuranceService.cancel(insuranceId));
    }
}
