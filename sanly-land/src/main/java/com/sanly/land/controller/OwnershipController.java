package com.sanly.land.controller;

import com.sanly.land.dto.request.AssignOwnershipRequest;
import com.sanly.land.dto.response.OwnershipResponse;
import com.sanly.land.service.OwnershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/land/ownership")
public class OwnershipController {
    private final OwnershipService ownershipService;

    public OwnershipController(OwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    @PostMapping
    public ResponseEntity<OwnershipResponse> assign(@Valid @RequestBody AssignOwnershipRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ownershipService.assign(req));
    }

    @GetMapping("/property/{cadastralNumber}")
    public List<OwnershipResponse> getByProperty(@PathVariable String cadastralNumber) {
        return ownershipService.findByProperty(cadastralNumber);
    }

    @GetMapping("/citizen/{nationalId}")
    public List<OwnershipResponse> getByCitizen(@PathVariable String nationalId) {
        return ownershipService.findByCitizen(nationalId);
    }

    // Internal endpoint: called by sanly-civil on death registration
    @PostMapping("/deceased")
    public ResponseEntity<Void> handleDeceased(@RequestBody Map<String, String> body) {
        ownershipService.handleDeceased(body.get("deceasedNationalId"));
        return ResponseEntity.ok().build();
    }
}
