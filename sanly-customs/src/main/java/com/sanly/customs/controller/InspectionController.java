package com.sanly.customs.controller;

import com.sanly.customs.dto.request.RecordInspectionRequest;
import com.sanly.customs.dto.response.InspectionResponse;
import com.sanly.customs.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customs/declarations/{declarationCode}/inspections")
@RequiredArgsConstructor
public class InspectionController {
    private final InspectionService inspectionService;

    @PostMapping
    public ResponseEntity<InspectionResponse> record(@PathVariable String declarationCode,
                                                      @RequestBody RecordInspectionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inspectionService.record(declarationCode, req));
    }

    @GetMapping
    public ResponseEntity<List<InspectionResponse>> list(@PathVariable String declarationCode) {
        return ResponseEntity.ok(inspectionService.findByDeclaration(declarationCode));
    }
}
