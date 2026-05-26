package com.sanly.police.service;

import com.sanly.police.client.BridgePublisherService;
import com.sanly.police.client.CitizenRegistryClient;
import com.sanly.police.client.NotificationClient;
import com.sanly.police.config.UserDetailsImpl;
import com.sanly.police.dto.request.CriminalRecordCreateRequest;
import com.sanly.police.dto.request.RecordStatusRequest;
import com.sanly.police.dto.response.CriminalRecordResponse;
import com.sanly.police.entity.CriminalRecord;
import com.sanly.police.exception.CriminalRecordNotFoundException;
import com.sanly.police.repository.CriminalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CriminalRecordServiceImpl {

    private final CriminalRecordRepository recordRepository;
    private final CitizenRegistryClient citizenRegistryClient;
    private final BridgePublisherService bridgePublisher;
    private final NotificationClient notificationClient;

    @Transactional
    public CriminalRecordResponse create(CriminalRecordCreateRequest req) {
        citizenRegistryClient.verify(req.getCitizenNationalId());
        Long officerId = authenticatedOfficerId();

        CriminalRecord record = CriminalRecord.builder()
                .citizenNationalId(req.getCitizenNationalId())
                .offenseType(req.getOffenseType())
                .offenseDate(req.getOffenseDate())
                .verdict(req.getVerdict())
                .sentenceDescription(req.getSentenceDescription())
                .courtName(req.getCourtName())
                .recordedByOfficerId(officerId)
                .expiresAt(req.getExpiresAt())
                .build();

        CriminalRecord saved = recordRepository.save(record);
        bridgePublisher.publishCriminalRecord(saved);

        notificationClient.send(req.getCitizenNationalId(), "CRIMINAL_RECORD_ADDED", "EN",
                java.util.Map.of(
                        "offenseType",  req.getOffenseType().name(),
                        "recordedAt",   saved.getOffenseDate() != null ? saved.getOffenseDate().toString() : "",
                        "officerBadge", String.valueOf(officerId)
                ));

        log.info("Criminal record {} created for NIN={}", saved.getRecordId(), req.getCitizenNationalId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CriminalRecordResponse getById(UUID id) {
        return toResponse(recordRepository.findById(id)
                .orElseThrow(() -> new CriminalRecordNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public List<CriminalRecordResponse> getByCitizen(String nationalId) {
        return recordRepository.findByCitizenNationalIdOrderByCreatedAtDesc(nationalId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public CriminalRecordResponse updateStatus(UUID id, RecordStatusRequest req) {
        CriminalRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new CriminalRecordNotFoundException(id));
        record.setStatus(req.getStatus());
        return toResponse(recordRepository.save(record));
    }

    private Long authenticatedOfficerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetailsImpl) auth.getPrincipal()).getOfficerId();
    }

    private CriminalRecordResponse toResponse(CriminalRecord r) {
        return CriminalRecordResponse.builder()
                .recordId(r.getRecordId()).citizenNationalId(r.getCitizenNationalId())
                .offenseType(r.getOffenseType()).offenseDate(r.getOffenseDate())
                .verdict(r.getVerdict()).sentenceDescription(r.getSentenceDescription())
                .courtName(r.getCourtName()).recordedByOfficerId(r.getRecordedByOfficerId())
                .createdAt(r.getCreatedAt()).expiresAt(r.getExpiresAt())
                .status(r.getStatus()).bridgePublished(r.isBridgePublished()).build();
    }
}
