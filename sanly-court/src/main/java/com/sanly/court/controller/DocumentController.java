package com.sanly.court.controller;

import com.sanly.court.dto.request.SubmitDocumentRequest;
import com.sanly.court.dto.response.DocumentResponse;
import com.sanly.court.dto.response.DocumentVerifyResponse;
import com.sanly.court.entity.DocumentStatus;
import com.sanly.court.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    // Submit document for a case
    @PostMapping("/api/v1/court/cases/{caseNumber}/documents")
    public ResponseEntity<DocumentResponse> submit(@PathVariable String caseNumber,
                                                    @RequestBody SubmitDocumentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.submit(caseNumber, req));
    }

    @GetMapping("/api/v1/court/cases/{caseNumber}/documents")
    public ResponseEntity<List<DocumentResponse>> listByCase(@PathVariable String caseNumber) {
        return ResponseEntity.ok(documentService.findByCase(caseNumber));
    }

    @GetMapping("/api/v1/court/documents/{documentCode}")
    public ResponseEntity<DocumentResponse> findByCode(@PathVariable String documentCode) {
        return ResponseEntity.ok(documentService.findByCode(documentCode));
    }

    // Public verify — permitted in SecurityConfig
    @GetMapping("/api/v1/court/documents/verify/{documentCode}")
    public ResponseEntity<DocumentVerifyResponse> verify(@PathVariable String documentCode) {
        return ResponseEntity.ok(documentService.verify(documentCode));
    }

    @PatchMapping("/api/v1/court/documents/{documentCode}/status")
    public ResponseEntity<DocumentResponse> updateStatus(@PathVariable String documentCode,
                                                          @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(documentService.updateStatus(documentCode, DocumentStatus.valueOf(body.get("status"))));
    }
}
