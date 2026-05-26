package com.sanly.customs.service;

import com.sanly.customs.config.UserDetailsImpl;
import com.sanly.customs.dto.request.RecordInspectionRequest;
import com.sanly.customs.dto.response.InspectionResponse;
import com.sanly.customs.entity.InspectionRecord;
import com.sanly.customs.exception.RecordNotFoundException;
import com.sanly.customs.repository.CustomsDeclarationRepository;
import com.sanly.customs.repository.InspectionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InspectionService {
    private final InspectionRecordRepository inspectionRepo;
    private final CustomsDeclarationRepository declarationRepo;

    @Transactional
    public InspectionResponse record(String declarationCode, RecordInspectionRequest req) {
        declarationRepo.findByDeclarationCode(declarationCode)
                .orElseThrow(() -> new RecordNotFoundException("Declaration not found: " + declarationCode));

        InspectionRecord record = InspectionRecord.builder()
                .declarationCode(declarationCode)
                .inspectorOfficerId(getOfficerId())
                .inspectionDate(req.inspectionDate())
                .inspectionType(req.inspectionType())
                .findings(req.findings())
                .result(req.result())
                .notes(req.notes())
                .build();
        return InspectionResponse.from(inspectionRepo.save(record));
    }

    public List<InspectionResponse> findByDeclaration(String declarationCode) {
        return inspectionRepo.findByDeclarationCodeOrderByCreatedAtDesc(declarationCode)
                .stream().map(InspectionResponse::from).toList();
    }

    private UUID getOfficerId() {
        try {
            Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (p instanceof UserDetailsImpl ud) return ud.getOfficerId();
        } catch (Exception ignored) {}
        return null;
    }
}
