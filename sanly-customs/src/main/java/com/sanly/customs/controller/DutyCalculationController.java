package com.sanly.customs.controller;

import com.sanly.customs.dto.request.CalculateDutiesRequest;
import com.sanly.customs.dto.response.DutyCalculationResponse;
import com.sanly.customs.service.DutyCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customs/declarations/{declarationCode}")
@RequiredArgsConstructor
public class DutyCalculationController {
    private final DutyCalculationService dutyService;

    @PostMapping("/calculate-duties")
    public ResponseEntity<DutyCalculationResponse> calculate(
            @PathVariable String declarationCode,
            @RequestBody(required = false) CalculateDutiesRequest req) {
        return ResponseEntity.ok(dutyService.calculate(declarationCode, req));
    }

    @GetMapping("/duties")
    public ResponseEntity<DutyCalculationResponse> getDuties(@PathVariable String declarationCode) {
        return ResponseEntity.ok(dutyService.getByDeclaration(declarationCode));
    }
}
