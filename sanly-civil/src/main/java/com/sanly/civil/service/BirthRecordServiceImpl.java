package com.sanly.civil.service;

import com.sanly.civil.client.BridgePublisherService;
import com.sanly.civil.client.CitizenRegistryClient;
import com.sanly.civil.client.NotificationClient;
import com.sanly.civil.service.LifeEventPublisherService;
import com.sanly.civil.dto.BirthRecordResponse;
import com.sanly.civil.dto.RegisterBirthRequest;
import com.sanly.civil.entity.BirthRecord;
import com.sanly.civil.entity.CertificateSequence;
import com.sanly.civil.exception.DuplicateResourceException;
import com.sanly.civil.repository.BirthRecordRepository;
import com.sanly.civil.repository.CertificateSequenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class BirthRecordServiceImpl {
    private static final String SEQ_TYPE = "BIRTH";

    private final BirthRecordRepository birthRecordRepository;
    private final CertificateSequenceRepository sequenceRepository;
    private final CitizenRegistryClient citizenRegistryClient;
    private final BridgePublisherService bridgePublisherService;
    private final NotificationClient notificationClient;
    private final LifeEventPublisherService lifeEventPublisher;

    public BirthRecordServiceImpl(BirthRecordRepository birthRecordRepository,
                                   CertificateSequenceRepository sequenceRepository,
                                   CitizenRegistryClient citizenRegistryClient,
                                   BridgePublisherService bridgePublisherService,
                                   NotificationClient notificationClient,
                                   LifeEventPublisherService lifeEventPublisher) {
        this.birthRecordRepository = birthRecordRepository;
        this.sequenceRepository = sequenceRepository;
        this.citizenRegistryClient = citizenRegistryClient;
        this.bridgePublisherService = bridgePublisherService;
        this.notificationClient = notificationClient;
        this.lifeEventPublisher = lifeEventPublisher;
    }

    @Transactional
    public BirthRecordResponse register(RegisterBirthRequest req) {
        if (birthRecordRepository.existsByChildNationalId(req.childNationalId())) {
            throw new DuplicateResourceException("Birth record already exists for: " + req.childNationalId());
        }
        citizenRegistryClient.verify(req.childNationalId());

        int year = LocalDate.now().getYear();
        String certNumber = generateCertificateNumber(SEQ_TYPE, year, "TM-BIRTH");

        BirthRecord record = new BirthRecord();
        record.setCertificateNumber(certNumber);
        record.setChildNationalId(req.childNationalId());
        record.setChildFirstName(req.childFirstName());
        record.setChildLastName(req.childLastName());
        record.setDateOfBirth(req.dateOfBirth());
        record.setPlaceOfBirth(req.placeOfBirth());
        record.setFatherNationalId(req.fatherNationalId());
        record.setFatherFullName(req.fatherFullName());
        record.setMotherNationalId(req.motherNationalId());
        record.setMotherFullName(req.motherFullName());
        record.setBridgePublished(false);

        BirthRecord saved = birthRecordRepository.save(record);
        bridgePublisherService.publishBirthRecord(saved.getId());

        // Notify both parents if they have NINs on file
        java.util.Map<String, String> meta = java.util.Map.of(
                "childFullName",    req.childFirstName() + " " + req.childLastName(),
                "dateOfBirth",      req.dateOfBirth().toString(),
                "certificateNumber", certNumber,
                "officerName",      "Civil Registry Officer"
        );
        if (req.fatherNationalId() != null) notificationClient.send(req.fatherNationalId(), "CIVIL_BIRTH_REGISTERED", "EN", meta);
        if (req.motherNationalId() != null) notificationClient.send(req.motherNationalId(), "CIVIL_BIRTH_REGISTERED", "EN", meta);

        // Life event automation: benefits, vaccination schedule, enrollment queue
        lifeEventPublisher.onBirthRegistered(saved);

        return BirthRecordResponse.from(saved);
    }

    public Page<BirthRecordResponse> findAll(Pageable pageable) {
        return birthRecordRepository.findAll(pageable).map(BirthRecordResponse::from);
    }

    public BirthRecordResponse findById(UUID id) {
        return BirthRecordResponse.from(birthRecordRepository.findById(id)
                .orElseThrow(() -> new com.sanly.civil.exception.RecordNotFoundException("Birth record not found: " + id)));
    }

    public BirthRecordResponse findByNationalId(String nationalId) {
        return BirthRecordResponse.from(birthRecordRepository.findByChildNationalId(nationalId)
                .orElseThrow(() -> new com.sanly.civil.exception.RecordNotFoundException("Birth record not found for: " + nationalId)));
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
