package com.sanly.tax.service;

import com.sanly.tax.client.BridgePublisherService;
import com.sanly.tax.client.CitizenRegistryClient;
import com.sanly.tax.config.UserDetailsImpl;
import com.sanly.tax.dto.request.TaxpayerRegisterRequest;
import com.sanly.tax.dto.response.ComplianceResponse;
import com.sanly.tax.dto.response.TaxpayerResponse;
import com.sanly.tax.entity.*;
import com.sanly.tax.exception.*;
import com.sanly.tax.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j @Service @RequiredArgsConstructor
public class TaxpayerServiceImpl {

    private final TaxpayerRecordRepository taxpayerRepository;
    private final TaxFilingRepository filingRepository;
    private final TaxIdSequenceRepository sequenceRepository;
    private final CitizenRegistryClient citizenRegistryClient;
    private final BridgePublisherService bridgePublisher;

    @Transactional
    public TaxpayerResponse register(TaxpayerRegisterRequest req) {
        if (taxpayerRepository.existsByCitizenNationalId(req.getCitizenNationalId())) {
            throw new DuplicateTaxpayerException(req.getCitizenNationalId());
        }
        citizenRegistryClient.verify(req.getCitizenNationalId());

        String taxId = generateTaxId();
        Long officerId = authenticatedOfficerId();

        TaxpayerRecord record = TaxpayerRecord.builder()
                .citizenNationalId(req.getCitizenNationalId())
                .taxId(taxId)
                .registrationDate(LocalDate.now())
                .taxpayerType(req.getTaxpayerType())
                .annualIncomeClass(req.getAnnualIncomeClass())
                .registeredByOfficerId(officerId)
                .build();

        TaxpayerRecord saved = taxpayerRepository.save(record);

        // Publish TAX_STATUS to bridge (async) — newly registered = ACTIVE
        bridgePublisher.publishTaxStatus(req.getCitizenNationalId(), taxId,
                req.getTaxpayerType().name(), "ACTIVE", null);

        log.info("Registered taxpayer NIN={} taxId={}", req.getCitizenNationalId(), taxId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaxpayerResponse getByNationalId(String nationalId) {
        return toResponse(taxpayerRepository.findByCitizenNationalId(nationalId)
                .orElseThrow(() -> new TaxpayerNotFoundException(nationalId)));
    }

    @Transactional(readOnly = true)
    public ComplianceResponse getCompliance(String nationalId) {
        TaxpayerRecord taxpayer = taxpayerRepository.findByCitizenNationalId(nationalId)
                .orElseThrow(() -> new TaxpayerNotFoundException(nationalId));

        Optional<TaxFiling> latestFiling = filingRepository
                .findTopByTaxIdOrderByTaxYearDescSubmittedAtDesc(taxpayer.getTaxId());

        if (latestFiling.isEmpty()) {
            return ComplianceResponse.builder().nationalId(nationalId)
                    .taxId(taxpayer.getTaxId()).complianceStatus("PENDING").build();
        }

        TaxFiling filing = latestFiling.get();
        String complianceStatus = filing.getFilingStatus() == FilingStatus.ACCEPTED
                ? "COMPLIANT" : "NON_COMPLIANT";

        return ComplianceResponse.builder()
                .nationalId(nationalId).taxId(taxpayer.getTaxId())
                .complianceStatus(complianceStatus)
                .latestFilingYear(filing.getTaxYear())
                .latestFilingStatus(filing.getFilingStatus().name())
                .build();
    }

    private String generateTaxId() {
        int year = LocalDate.now().getYear();
        sequenceRepository.insertIfNotExists(year);
        TaxIdSequence seq = sequenceRepository.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Tax ID sequence not found"));
        int next = seq.getLastCounter() + 1;
        seq.setLastCounter(next);
        sequenceRepository.save(seq);
        return "TM-TAX-%d%06d".formatted(year, next);
    }

    private Long authenticatedOfficerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetailsImpl) auth.getPrincipal()).getOfficerId();
    }

    private TaxpayerResponse toResponse(TaxpayerRecord r) {
        return TaxpayerResponse.builder()
                .recordId(r.getRecordId()).citizenNationalId(r.getCitizenNationalId())
                .taxId(r.getTaxId()).registrationDate(r.getRegistrationDate())
                .taxpayerType(r.getTaxpayerType()).status(r.getStatus())
                .annualIncomeClass(r.getAnnualIncomeClass()).createdAt(r.getCreatedAt()).build();
    }
}
