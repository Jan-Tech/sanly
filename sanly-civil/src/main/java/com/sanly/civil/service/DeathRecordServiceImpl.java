package com.sanly.civil.service;

import com.sanly.civil.client.BridgePublisherService;
import com.sanly.civil.client.CitizenRegistryClient;
import com.sanly.civil.client.NotificationClient;
import com.sanly.civil.service.LifeEventPublisherService;
import com.sanly.civil.dto.DeathRecordResponse;
import com.sanly.civil.dto.RegisterDeathRequest;
import com.sanly.civil.entity.CertificateSequence;
import com.sanly.civil.entity.DeathRecord;
import com.sanly.civil.exception.DuplicateResourceException;
import com.sanly.civil.exception.RecordNotFoundException;
import com.sanly.civil.repository.CertificateSequenceRepository;
import com.sanly.civil.repository.DeathRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class DeathRecordServiceImpl {
    private static final String SEQ_TYPE = "DEATH";

    private final DeathRecordRepository deathRecordRepository;
    private final CertificateSequenceRepository sequenceRepository;
    private final CitizenRegistryClient citizenRegistryClient;
    private final BridgePublisherService bridgePublisherService;
    private final NotificationClient notificationClient;
    private final LifeEventPublisherService lifeEventPublisher;

    public DeathRecordServiceImpl(DeathRecordRepository deathRecordRepository,
                                   CertificateSequenceRepository sequenceRepository,
                                   CitizenRegistryClient citizenRegistryClient,
                                   BridgePublisherService bridgePublisherService,
                                   NotificationClient notificationClient,
                                   LifeEventPublisherService lifeEventPublisher) {
        this.deathRecordRepository = deathRecordRepository;
        this.sequenceRepository = sequenceRepository;
        this.citizenRegistryClient = citizenRegistryClient;
        this.bridgePublisherService = bridgePublisherService;
        this.notificationClient = notificationClient;
        this.lifeEventPublisher = lifeEventPublisher;
    }

    @Transactional
    public DeathRecordResponse register(RegisterDeathRequest req) {
        if (deathRecordRepository.existsByDeceasedNationalId(req.deceasedNationalId())) {
            throw new DuplicateResourceException("Death record already exists for: " + req.deceasedNationalId());
        }

        // Verify citizen exists — this also ensures we have a valid nationalId to mark DECEASED
        citizenRegistryClient.verify(req.deceasedNationalId());

        // SYNCHRONOUS — fails the whole registration if citizen registry is unreachable
        citizenRegistryClient.markDeceased(req.deceasedNationalId());

        int year = LocalDate.now().getYear();
        String certNumber = generateCertificateNumber(SEQ_TYPE, year, "TM-DEATH");

        DeathRecord record = new DeathRecord();
        record.setCertificateNumber(certNumber);
        record.setDeceasedNationalId(req.deceasedNationalId());
        record.setDeceasedFullName(req.deceasedFullName());
        record.setDateOfDeath(req.dateOfDeath());
        record.setPlaceOfDeath(req.placeOfDeath());
        record.setDeathCause(req.deathCause());
        record.setBridgePublished(false);

        DeathRecord saved = deathRecordRepository.save(record);
        bridgePublisherService.publishDeathRecord(saved.getId());

        notificationClient.send(req.deceasedNationalId(), "CIVIL_DEATH_REGISTERED", "EN",
                java.util.Map.of(
                        "deceasedFullName",  req.deceasedFullName(),
                        "dateOfDeath",       req.dateOfDeath().toString(),
                        "certificateNumber", certNumber
                ));

        // Life event automation: cancel benefits, revoke licenses, suspend businesses
        lifeEventPublisher.onDeathRegistered(saved);

        return DeathRecordResponse.from(saved);
    }

    public Page<DeathRecordResponse> findAll(Pageable pageable) {
        return deathRecordRepository.findAll(pageable).map(DeathRecordResponse::from);
    }

    public DeathRecordResponse findById(UUID id) {
        return DeathRecordResponse.from(deathRecordRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Death record not found: " + id)));
    }

    public DeathRecordResponse findByNationalId(String nationalId) {
        return DeathRecordResponse.from(deathRecordRepository.findByDeceasedNationalId(nationalId)
                .orElseThrow(() -> new RecordNotFoundException("Death record not found for: " + nationalId)));
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
