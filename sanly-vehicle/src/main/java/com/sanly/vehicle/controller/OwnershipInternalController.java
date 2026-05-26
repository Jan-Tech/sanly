package com.sanly.vehicle.controller;

import com.sanly.vehicle.dto.request.DeceasedOwnershipRequest;
import com.sanly.vehicle.service.OwnershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/vehicle/ownership")
@RequiredArgsConstructor
public class OwnershipInternalController {
    private final OwnershipService ownershipService;

    @Value("${sanly.life-event.service-key:change-me-life-event-key}")
    private String expectedServiceKey;

    @PostMapping("/deceased")
    public ResponseEntity<Void> handleDeceased(
            @RequestHeader("X-Life-Event-Key") String serviceKey,
            @Valid @RequestBody DeceasedOwnershipRequest req) {
        if (!expectedServiceKey.equals(serviceKey)) {
            log.warn("Rejected deceased ownership call — invalid service key");
            return ResponseEntity.status(403).build();
        }
        ownershipService.handleDeceased(req.getDeceasedNationalId());
        return ResponseEntity.ok().build();
    }
}
