package com.sanly.tax.service;

import com.sanly.tax.client.BridgePublisherService;
import com.sanly.tax.client.NotificationClient;
import com.sanly.tax.config.UserDetailsImpl;
import com.sanly.tax.dto.request.FilingProcessRequest;
import com.sanly.tax.dto.request.TaxFilingCreateRequest;
import com.sanly.tax.dto.response.TaxFilingResponse;
import com.sanly.tax.entity.*;
import com.sanly.tax.exception.*;
import com.sanly.tax.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class TaxFilingServiceImpl {

    private final TaxFilingRepository filingRepository;
    private final TaxpayerRecordRepository taxpayerRepository;
    private final BridgePublisherService bridgePublisher;
    private final NotificationClient notificationClient;

    @Transactional
    public TaxFilingResponse create(TaxFilingCreateRequest req) {
        TaxpayerRecord taxpayer = taxpayerRepository.findByTaxId(req.getTaxId())
                .orElseThrow(() -> new TaxpayerNotFoundException(req.getTaxId()));

        TaxFiling filing = TaxFiling.builder()
                .taxId(req.getTaxId())
                .taxYear(req.getTaxYear())
                .filingStatus(FilingStatus.SUBMITTED)
                .declaredIncome(req.getDeclaredIncome())
                .taxDue(req.getTaxDue())
                .taxPaid(req.getTaxPaid())
                .submittedAt(LocalDateTime.now())
                .notes(req.getNotes())
                .build();

        return toResponse(filingRepository.save(filing));
    }

    @Transactional(readOnly = true)
    public TaxFilingResponse getById(UUID id) {
        return toResponse(filingRepository.findById(id)
                .orElseThrow(() -> new TaxFilingNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public List<TaxFilingResponse> getByTaxId(String taxId) {
        return filingRepository.findByTaxIdOrderByTaxYearDesc(taxId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public TaxFilingResponse process(UUID id, FilingProcessRequest req) {
        TaxFiling filing = filingRepository.findById(id)
                .orElseThrow(() -> new TaxFilingNotFoundException(id));

        if (filing.getFilingStatus() == FilingStatus.ACCEPTED
                || filing.getFilingStatus() == FilingStatus.REJECTED) {
            throw new InvalidFilingStateException(
                    "Filing already processed with status: " + filing.getFilingStatus());
        }

        if (req.getFilingStatus() != FilingStatus.ACCEPTED
                && req.getFilingStatus() != FilingStatus.REJECTED) {
            throw new InvalidFilingStateException(
                    "Process endpoint only accepts ACCEPTED or REJECTED status");
        }

        Long officerId = authenticatedOfficerId();
        filing.setFilingStatus(req.getFilingStatus());
        filing.setProcessedAt(LocalDateTime.now());
        filing.setProcessedByOfficerId(officerId);
        if (req.getNotes() != null) filing.setNotes(req.getNotes());

        TaxFiling saved = filingRepository.save(filing);

        // Re-publish TAX_STATUS to bridge with updated compliance
        TaxpayerRecord taxpayer = taxpayerRepository.findByTaxId(filing.getTaxId()).orElse(null);
        if (taxpayer != null) {
            String compliance = saved.getFilingStatus() == FilingStatus.ACCEPTED
                    ? "COMPLIANT" : "NON_COMPLIANT";
            bridgePublisher.publishTaxStatus(taxpayer.getCitizenNationalId(), filing.getTaxId(),
                    taxpayer.getTaxpayerType().name(), compliance, filing.getTaxYear());

            notificationClient.send(taxpayer.getCitizenNationalId(), "TAX_STATUS_CHANGED", "EN",
                    java.util.Map.of(
                            "newStatus", compliance,
                            "changedAt", saved.getProcessedAt() != null ? saved.getProcessedAt().toString() : "",
                            "taxId",     filing.getTaxId()
                    ));
        }

        log.info("Filing {} processed as {}", id, req.getFilingStatus());
        return toResponse(saved);
    }

    private Long authenticatedOfficerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetailsImpl) auth.getPrincipal()).getOfficerId();
    }

    private TaxFilingResponse toResponse(TaxFiling f) {
        return TaxFilingResponse.builder()
                .filingId(f.getFilingId()).taxId(f.getTaxId()).taxYear(f.getTaxYear())
                .filingStatus(f.getFilingStatus()).declaredIncome(f.getDeclaredIncome())
                .taxDue(f.getTaxDue()).taxPaid(f.getTaxPaid())
                .submittedAt(f.getSubmittedAt()).processedAt(f.getProcessedAt())
                .notes(f.getNotes()).build();
    }
}
