package com.sanly.civil.service;

import com.sanly.civil.client.BridgePublisherService;
import com.sanly.civil.client.CitizenRegistryClient;
import com.sanly.civil.client.NotificationClient;
import com.sanly.civil.service.LifeEventPublisherService;
import com.sanly.civil.dto.MarriageRecordResponse;
import com.sanly.civil.dto.RegisterMarriageRequest;
import com.sanly.civil.entity.CertificateSequence;
import com.sanly.civil.entity.MarriageRecord;
import com.sanly.civil.entity.MarriageStatus;
import com.sanly.civil.exception.DuplicateResourceException;
import com.sanly.civil.exception.InvalidOperationException;
import com.sanly.civil.exception.RecordNotFoundException;
import com.sanly.civil.repository.CertificateSequenceRepository;
import com.sanly.civil.repository.MarriageRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class MarriageRecordServiceImpl {
    private static final String SEQ_TYPE = "MARR";

    private final MarriageRecordRepository marriageRecordRepository;
    private final CertificateSequenceRepository sequenceRepository;
    private final CitizenRegistryClient citizenRegistryClient;
    private final BridgePublisherService bridgePublisherService;
    private final NotificationClient notificationClient;
    private final LifeEventPublisherService lifeEventPublisher;

    public MarriageRecordServiceImpl(MarriageRecordRepository marriageRecordRepository,
                                      CertificateSequenceRepository sequenceRepository,
                                      CitizenRegistryClient citizenRegistryClient,
                                      BridgePublisherService bridgePublisherService,
                                      NotificationClient notificationClient,
                                      LifeEventPublisherService lifeEventPublisher) {
        this.marriageRecordRepository = marriageRecordRepository;
        this.sequenceRepository = sequenceRepository;
        this.citizenRegistryClient = citizenRegistryClient;
        this.bridgePublisherService = bridgePublisherService;
        this.notificationClient = notificationClient;
        this.lifeEventPublisher = lifeEventPublisher;
    }

    @Transactional
    public MarriageRecordResponse register(RegisterMarriageRequest req) {
        if (req.spouse1NationalId().equals(req.spouse2NationalId())) {
            throw new InvalidOperationException("Spouses cannot have the same national ID");
        }
        if (!marriageRecordRepository.findActiveBySpouseNationalId(req.spouse1NationalId()).isEmpty() ||
                !marriageRecordRepository.findActiveBySpouseNationalId(req.spouse2NationalId()).isEmpty()) {
            throw new DuplicateResourceException("One or both spouses already have an active marriage record");
        }
        citizenRegistryClient.verify(req.spouse1NationalId());
        citizenRegistryClient.verify(req.spouse2NationalId());

        int year = LocalDate.now().getYear();
        String certNumber = generateCertificateNumber(SEQ_TYPE, year, "TM-MARR");

        MarriageRecord record = new MarriageRecord();
        record.setCertificateNumber(certNumber);
        record.setSpouse1NationalId(req.spouse1NationalId());
        record.setSpouse1FullName(req.spouse1FullName());
        record.setSpouse2NationalId(req.spouse2NationalId());
        record.setSpouse2FullName(req.spouse2FullName());
        record.setMarriageDate(req.marriageDate());
        record.setStatus(MarriageStatus.ACTIVE);
        record.setBridgePublished(false);

        MarriageRecord saved = marriageRecordRepository.save(record);
        bridgePublisherService.publishMarriageRecord(saved.getId());

        // Notify both spouses
        java.util.Map<String, String> meta = java.util.Map.of(
                "marriageDate",      req.marriageDate().toString(),
                "certificateNumber", certNumber,
                "spouseNin",         req.spouse2NationalId()
        );
        notificationClient.send(req.spouse1NationalId(), "CIVIL_MARRIAGE_REGISTERED", "EN", meta);
        notificationClient.send(req.spouse2NationalId(), "CIVIL_MARRIAGE_REGISTERED", "EN",
                java.util.Map.of(
                        "marriageDate",      req.marriageDate().toString(),
                        "certificateNumber", certNumber,
                        "spouseNin",         req.spouse1NationalId()
                ));

        // Life event automation: update marital status in tax, notify about tax implications
        lifeEventPublisher.onMarriageRegistered(saved);

        return MarriageRecordResponse.from(saved);
    }

    @Transactional
    public MarriageRecordResponse dissolve(UUID id) {
        MarriageRecord record = marriageRecordRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Marriage record not found: " + id));
        if (record.getStatus() != MarriageStatus.ACTIVE) {
            throw new InvalidOperationException("Marriage is not active");
        }
        record.setStatus(MarriageStatus.DISSOLVED);
        record.setDissolutionDate(LocalDate.now());
        record.setBridgePublished(false);
        MarriageRecord saved = marriageRecordRepository.save(record);
        bridgePublisherService.publishMarriageRecord(saved.getId());

        // Life event automation: update marital status to DIVORCED in tax
        lifeEventPublisher.onMarriageDissolved(saved);

        return MarriageRecordResponse.from(saved);
    }

    public Page<MarriageRecordResponse> findAll(Pageable pageable) {
        return marriageRecordRepository.findAll(pageable).map(MarriageRecordResponse::from);
    }

    public MarriageRecordResponse findById(UUID id) {
        return MarriageRecordResponse.from(marriageRecordRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Marriage record not found: " + id)));
    }

    @Transactional
    protected String generateCertificateNumber(String seqType, int year, String prefix) {
        sequenceRepository.insertIfNotExists(seqType, year);
        CertificateSequence seq = sequenceRepository.findBySeqTypeAndYearWithLock(seqType, year)
                .orElseThrow(() -> new IllegalStateException("Sequence not found"));
        long next = seq.getLastValue() + 1;
        seq.setLastValue(next);
        sequenceRepository.save(seq);
        return String.format("%s-%d%06d", prefix, year, next);
    }
}
