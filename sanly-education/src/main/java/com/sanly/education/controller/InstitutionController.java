package com.sanly.education.controller;

import com.sanly.education.dto.request.RegisterInstitutionRequest;
import com.sanly.education.dto.response.InstitutionResponse;
import com.sanly.education.entity.InstitutionStatus;
import com.sanly.education.service.InstitutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/education/institutions")
public class InstitutionController {
    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @PostMapping
    public ResponseEntity<InstitutionResponse> register(@Valid @RequestBody RegisterInstitutionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(institutionService.register(req));
    }

    @GetMapping
    public List<InstitutionResponse> listAll() {
        return institutionService.findAll();
    }

    @GetMapping("/{institutionCode}")
    public InstitutionResponse getByCode(@PathVariable String institutionCode) {
        return institutionService.findByCode(institutionCode);
    }

    @PatchMapping("/{institutionCode}/status")
    public InstitutionResponse updateStatus(@PathVariable String institutionCode,
                                            @RequestBody Map<String, String> body) {
        InstitutionStatus status = InstitutionStatus.valueOf(body.get("status"));
        return institutionService.updateStatus(institutionCode, status);
    }
}
