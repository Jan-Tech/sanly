package com.sanly.land.controller;

import com.sanly.land.dto.request.RegisterPropertyRequest;
import com.sanly.land.dto.response.PropertyDetailResponse;
import com.sanly.land.dto.response.PropertyResponse;
import com.sanly.land.entity.PropertyStatus;
import com.sanly.land.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/land/properties")
public class PropertyController {
    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> register(@Valid @RequestBody RegisterPropertyRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.register(req));
    }

    @GetMapping("/{cadastralNumber}")
    public PropertyDetailResponse getByCode(@PathVariable String cadastralNumber) {
        return propertyService.findByCode(cadastralNumber);
    }

    @GetMapping("/owner/{nationalId}")
    public List<PropertyDetailResponse> getByOwner(@PathVariable String nationalId) {
        return propertyService.findByOwner(nationalId);
    }

    @GetMapping("/verify/{cadastralNumber}")
    public PropertyDetailResponse verify(@PathVariable String cadastralNumber) {
        return propertyService.verifyPublic(cadastralNumber);
    }

    @PatchMapping("/{cadastralNumber}/status")
    public PropertyResponse updateStatus(@PathVariable String cadastralNumber,
                                         @RequestBody Map<String, String> body) {
        return propertyService.updateStatus(cadastralNumber, PropertyStatus.valueOf(body.get("status")));
    }
}
