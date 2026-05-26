package com.sanly.land.controller;

import com.sanly.land.dto.request.SubmitTransferRequest;
import com.sanly.land.dto.response.TransferApplicationResponse;
import com.sanly.land.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/land/transfers")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferApplicationResponse> submit(
            @Valid @RequestBody SubmitTransferRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferService.submit(req));
    }

    @GetMapping("/{applicationId}")
    public TransferApplicationResponse getById(@PathVariable UUID applicationId) {
        return transferService.findById(applicationId);
    }

    @GetMapping("/property/{cadastralNumber}")
    public List<TransferApplicationResponse> getByProperty(@PathVariable String cadastralNumber) {
        return transferService.findByProperty(cadastralNumber);
    }

    @GetMapping("/citizen/{nationalId}")
    public List<TransferApplicationResponse> getByCitizen(@PathVariable String nationalId) {
        return transferService.findByCitizen(nationalId);
    }

    @GetMapping("/pending")
    public List<TransferApplicationResponse> getPending() {
        return transferService.findPending();
    }

    @PostMapping("/{applicationId}/approve")
    public TransferApplicationResponse approve(@PathVariable UUID applicationId, Authentication auth) {
        return transferService.approve(applicationId, auth);
    }

    @PostMapping("/{applicationId}/reject")
    public TransferApplicationResponse reject(@PathVariable UUID applicationId,
                                               @RequestBody Map<String, String> body,
                                               Authentication auth) {
        return transferService.reject(applicationId, body.get("reason"), auth);
    }
}
