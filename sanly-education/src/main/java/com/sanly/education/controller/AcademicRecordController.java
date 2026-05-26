package com.sanly.education.controller;

import com.sanly.education.dto.request.AddAcademicRecordRequest;
import com.sanly.education.dto.response.AcademicRecordResponse;
import com.sanly.education.service.AcademicRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/education/records")
public class AcademicRecordController {
    private final AcademicRecordService recordService;

    public AcademicRecordController(AcademicRecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    public ResponseEntity<AcademicRecordResponse> add(@Valid @RequestBody AddAcademicRecordRequest req,
                                                      Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.addRecord(req, auth));
    }

    @GetMapping("/citizen/{nationalId}")
    public List<AcademicRecordResponse> getByCitizen(@PathVariable String nationalId) {
        return recordService.findByCitizen(nationalId);
    }
}
