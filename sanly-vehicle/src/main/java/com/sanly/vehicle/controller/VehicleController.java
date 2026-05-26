package com.sanly.vehicle.controller;

import com.sanly.vehicle.dto.request.RegisterVehicleRequest;
import com.sanly.vehicle.dto.request.UpdateVehicleStatusRequest;
import com.sanly.vehicle.dto.response.VehicleResponse;
import com.sanly.vehicle.dto.response.VehicleVerifyResponse;
import com.sanly.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicle/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponse> register(@Valid @RequestBody RegisterVehicleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.register(req));
    }

    @GetMapping("/{plateNumber}")
    public ResponseEntity<VehicleResponse> getByPlate(@PathVariable String plateNumber) {
        return ResponseEntity.ok(vehicleService.getByPlate(plateNumber));
    }

    @GetMapping("/vin/{vin}")
    public ResponseEntity<VehicleResponse> getByVin(@PathVariable String vin) {
        return ResponseEntity.ok(vehicleService.getByVin(vin));
    }

    @GetMapping("/owner/{nationalId}")
    public ResponseEntity<List<VehicleResponse>> getByOwner(@PathVariable String nationalId) {
        return ResponseEntity.ok(vehicleService.getByOwner(nationalId));
    }

    @GetMapping("/verify/{plateNumber}")
    public ResponseEntity<VehicleVerifyResponse> verify(@PathVariable String plateNumber) {
        return ResponseEntity.ok(vehicleService.verify(plateNumber));
    }

    @PatchMapping("/{plateNumber}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> updateStatus(@PathVariable String plateNumber,
                                                         @Valid @RequestBody UpdateVehicleStatusRequest req) {
        return ResponseEntity.ok(vehicleService.updateStatus(plateNumber, req));
    }
}
