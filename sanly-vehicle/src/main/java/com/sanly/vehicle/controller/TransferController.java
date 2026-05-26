package com.sanly.vehicle.controller;

import com.sanly.vehicle.dto.request.CreateTransferRequest;
import com.sanly.vehicle.dto.request.RejectTransferRequest;
import com.sanly.vehicle.dto.response.OwnershipResponse;
import com.sanly.vehicle.dto.response.TransferApplicationResponse;
import com.sanly.vehicle.entity.TransferStatus;
import com.sanly.vehicle.service.OwnershipService;
import com.sanly.vehicle.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicle")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;
    private final OwnershipService ownershipService;

    @PostMapping("/transfers")
    public ResponseEntity<TransferApplicationResponse> submit(@Valid @RequestBody CreateTransferRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferService.submit(req));
    }

    @GetMapping("/transfers/{applicationId}")
    public ResponseEntity<TransferApplicationResponse> getById(@PathVariable Long applicationId) {
        return ResponseEntity.ok(transferService.findById(applicationId));
    }

    @GetMapping("/transfers/vehicle/{plateNumber}")
    public ResponseEntity<List<TransferApplicationResponse>> getByPlate(@PathVariable String plateNumber) {
        return ResponseEntity.ok(transferService.findByPlate(plateNumber));
    }

    @GetMapping("/transfers")
    public ResponseEntity<List<TransferApplicationResponse>> getByStatus(
            @RequestParam(defaultValue = "PENDING") TransferStatus status) {
        return ResponseEntity.ok(transferService.findByStatus(status));
    }

    @PostMapping("/transfers/{applicationId}/approve")
    public ResponseEntity<TransferApplicationResponse> approve(@PathVariable Long applicationId) {
        return ResponseEntity.ok(transferService.approve(applicationId));
    }

    @PostMapping("/transfers/{applicationId}/reject")
    public ResponseEntity<TransferApplicationResponse> reject(@PathVariable Long applicationId,
                                                               @Valid @RequestBody RejectTransferRequest req) {
        return ResponseEntity.ok(transferService.reject(applicationId, req));
    }

    @GetMapping("/ownership/vehicle/{plateNumber}")
    public ResponseEntity<List<OwnershipResponse>> getOwnershipHistory(@PathVariable String plateNumber) {
        return ResponseEntity.ok(ownershipService.findByPlate(plateNumber));
    }
}
