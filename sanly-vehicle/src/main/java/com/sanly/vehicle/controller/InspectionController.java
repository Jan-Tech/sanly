package com.sanly.vehicle.controller;

import com.sanly.vehicle.dto.request.RecordInspectionRequest;
import com.sanly.vehicle.dto.response.InspectionResponse;
import com.sanly.vehicle.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicle/inspections")
@RequiredArgsConstructor
public class InspectionController {
    private final InspectionService inspectionService;

    @PostMapping
    public ResponseEntity<InspectionResponse> record(@Valid @RequestBody RecordInspectionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inspectionService.record(req));
    }

    @GetMapping("/vehicle/{plateNumber}")
    public ResponseEntity<List<InspectionResponse>> findByPlate(@PathVariable String plateNumber) {
        return ResponseEntity.ok(inspectionService.findByPlate(plateNumber));
    }

    @GetMapping("/due")
    public ResponseEntity<List<InspectionResponse>> findDue() {
        return ResponseEntity.ok(inspectionService.findDueWithin30Days());
    }
}
