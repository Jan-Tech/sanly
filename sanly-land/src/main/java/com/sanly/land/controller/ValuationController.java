package com.sanly.land.controller;

import com.sanly.land.dto.request.AddValuationRequest;
import com.sanly.land.dto.response.ValuationResponse;
import com.sanly.land.service.ValuationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/land/valuations")
public class ValuationController {
    private final ValuationService valuationService;

    public ValuationController(ValuationService valuationService) {
        this.valuationService = valuationService;
    }

    @PostMapping
    public ResponseEntity<ValuationResponse> add(@Valid @RequestBody AddValuationRequest req,
                                                  Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(valuationService.add(req, auth));
    }

    @GetMapping("/property/{cadastralNumber}")
    public List<ValuationResponse> getByProperty(@PathVariable String cadastralNumber) {
        return valuationService.findByProperty(cadastralNumber);
    }
}
