package com.sanly.court.service;

import com.sanly.court.dto.request.SubmitDocumentRequest;
import com.sanly.court.dto.response.DocumentResponse;
import com.sanly.court.dto.response.DocumentVerifyResponse;
import com.sanly.court.entity.CourtDocument;
import com.sanly.court.entity.DocumentSequence;
import com.sanly.court.entity.DocumentStatus;
import com.sanly.court.exception.InvalidOperationException;
import com.sanly.court.exception.RecordNotFoundException;
import com.sanly.court.repository.CourtCaseRepository;
import com.sanly.court.repository.CourtDocumentRepository;
import com.sanly.court.repository.DocumentSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final CourtDocumentRepository docRepo;
    private final DocumentSequenceRepository seqRepo;
    private final CourtCaseRepository caseRepo;
    private final DocumentSigningService signingService;

    @Transactional
    public DocumentResponse submit(String caseNumber, SubmitDocumentRequest req) {
        var courtCase = caseRepo.findByCaseNumber(caseNumber)
                .orElseThrow(() -> new RecordNotFoundException("Case not found: " + caseNumber));

        // Validate submitter is party to the case
        String submitter = req.submittedByNationalId();
        if (!submitter.equals(courtCase.getPlaintiffNationalId())
                && !submitter.equals(courtCase.getDefendantNationalId())) {
            throw new InvalidOperationException("Submitter is not a party to case " + caseNumber);
        }

        LocalDateTime submittedAt = LocalDateTime.now();
        String signature = signingService.computeSignature(req.content(), submitter, submittedAt);

        CourtDocument doc = CourtDocument.builder()
                .documentCode(generateDocCode())
                .caseNumber(caseNumber)
                .documentType(req.documentType())
                .title(req.title())
                .content(req.content())
                .submittedByNationalId(submitter)
                .submittedAt(submittedAt)
                .digitalSignature(signature)
                .status(DocumentStatus.SUBMITTED)
                .build();
        return DocumentResponse.from(docRepo.save(doc));
    }

    public List<DocumentResponse> findByCase(String caseNumber) {
        return docRepo.findByCaseNumber(caseNumber).stream().map(DocumentResponse::from).toList();
    }

    public DocumentResponse findByCode(String documentCode) {
        return docRepo.findByDocumentCode(documentCode)
                .map(DocumentResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("Document not found: " + documentCode));
    }

    // Public — recompute hash and compare
    public DocumentVerifyResponse verify(String documentCode) {
        var doc = docRepo.findByDocumentCode(documentCode)
                .orElseThrow(() -> new RecordNotFoundException("Document not found: " + documentCode));

        boolean valid = signingService.verifySignature(
                doc.getContent(), doc.getSubmittedByNationalId(), doc.getSubmittedAt(), doc.getDigitalSignature());

        return new DocumentVerifyResponse(valid, doc.getDocumentCode(), doc.getSubmittedByNationalId(),
                doc.getSubmittedAt(), doc.getStatus().name(),
                valid ? "Document signature is valid" : "Document signature INVALID — content may have been tampered");
    }

    @Transactional
    public DocumentResponse updateStatus(String documentCode, DocumentStatus status) {
        CourtDocument doc = docRepo.findByDocumentCode(documentCode)
                .orElseThrow(() -> new RecordNotFoundException("Document not found: " + documentCode));
        doc.setStatus(status);
        return DocumentResponse.from(docRepo.save(doc));
    }

    protected String generateDocCode() {
        int year = LocalDate.now().getYear();
        seqRepo.insertIfNotExists(year);
        DocumentSequence seq = seqRepo.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Sequence missing for year " + year));
        int next = seq.getNextValue();
        seq.setNextValue(next + 1);
        seqRepo.save(seq);
        return "TM-DOC-%d%06d".formatted(year, next);
    }
}
