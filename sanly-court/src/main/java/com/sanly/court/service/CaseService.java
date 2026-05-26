package com.sanly.court.service;

import com.sanly.court.client.CitizenRegistryClient;
import com.sanly.court.client.NotificationClient;
import com.sanly.court.config.UserDetailsImpl;
import com.sanly.court.dto.request.AssignJudgeRequest;
import com.sanly.court.dto.request.FileCaseRequest;
import com.sanly.court.dto.request.ScheduleHearingRequest;
import com.sanly.court.dto.request.UpdateCaseStatusRequest;
import com.sanly.court.dto.response.CaseResponse;
import com.sanly.court.entity.CaseSequence;
import com.sanly.court.entity.CaseStatus;
import com.sanly.court.entity.CourtCase;
import com.sanly.court.exception.InvalidOperationException;
import com.sanly.court.exception.RecordNotFoundException;
import com.sanly.court.repository.CaseSequenceRepository;
import com.sanly.court.repository.CourtCaseRepository;
import com.sanly.court.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseService {

    private final CourtCaseRepository caseRepo;
    private final CaseSequenceRepository seqRepo;
    private final CourtRepository courtRepo;
    private final CitizenRegistryClient citizenClient;
    private final NotificationClient notificationClient;

    @Transactional
    public CaseResponse file(FileCaseRequest req) {
        courtRepo.findByCourtCode(req.courtCode())
                .orElseThrow(() -> new RecordNotFoundException("Court not found: " + req.courtCode()));
        citizenClient.verify(req.plaintiffNationalId());
        citizenClient.verify(req.defendantNationalId());

        CourtCase courtCase = CourtCase.builder()
                .caseNumber(generateCaseNumber())
                .courtCode(req.courtCode())
                .caseType(req.caseType())
                .plaintiffNationalId(req.plaintiffNationalId())
                .defendantNationalId(req.defendantNationalId())
                .summary(req.summary())
                .notes(req.notes())
                .status(CaseStatus.FILED)
                .build();
        caseRepo.save(courtCase);

        String courtName = courtRepo.findByCourtCode(req.courtCode()).map(c -> c.getName()).orElse(req.courtCode());
        Map<String, String> meta = Map.of(
                "caseNumber", courtCase.getCaseNumber(),
                "courtName", courtName,
                "caseType", req.caseType().name());

        notificationClient.send(req.plaintiffNationalId(), "COURT_CASE_FILED", "TK", meta);
        notificationClient.send(req.defendantNationalId(), "COURT_CASE_FILED", "TK", meta);

        return CaseResponse.from(courtCase);
    }

    @Transactional
    public CaseResponse updateStatus(String caseNumber, UpdateCaseStatusRequest req) {
        CourtCase courtCase = caseRepo.findByCaseNumberWithLock(caseNumber)
                .orElseThrow(() -> new RecordNotFoundException("Case not found: " + caseNumber));
        courtCase.setStatus(req.status());
        if (req.notes() != null) courtCase.setNotes(req.notes());
        if (req.status() == CaseStatus.CLOSED || req.status() == CaseStatus.DISMISSED)
            courtCase.setClosedAt(LocalDateTime.now());
        return CaseResponse.from(caseRepo.save(courtCase));
    }

    @Transactional
    public CaseResponse assignJudge(String caseNumber, AssignJudgeRequest req) {
        CourtCase courtCase = caseRepo.findByCaseNumberWithLock(caseNumber)
                .orElseThrow(() -> new RecordNotFoundException("Case not found: " + caseNumber));
        courtCase.setAssignedJudgeOfficerId(req.judgeOfficerId());
        if (courtCase.getStatus() == CaseStatus.FILED)
            courtCase.setStatus(CaseStatus.UNDER_REVIEW);
        return CaseResponse.from(caseRepo.save(courtCase));
    }

    @Transactional
    public CaseResponse scheduleHearing(String caseNumber, ScheduleHearingRequest req) {
        CourtCase courtCase = caseRepo.findByCaseNumberWithLock(caseNumber)
                .orElseThrow(() -> new RecordNotFoundException("Case not found: " + caseNumber));
        courtCase.setHearingDate(req.hearingDate());
        courtCase.setStatus(CaseStatus.HEARING_SCHEDULED);
        return CaseResponse.from(caseRepo.save(courtCase));
    }

    @Transactional
    public CaseResponse appeal(String caseNumber) {
        CourtCase courtCase = caseRepo.findByCaseNumberWithLock(caseNumber)
                .orElseThrow(() -> new RecordNotFoundException("Case not found: " + caseNumber));
        if (courtCase.getStatus() != CaseStatus.DECIDED)
            throw new InvalidOperationException("Only DECIDED cases can be appealed");
        courtCase.setStatus(CaseStatus.APPEALED);
        return CaseResponse.from(caseRepo.save(courtCase));
    }

    public CaseResponse findByNumber(String caseNumber) {
        return caseRepo.findByCaseNumber(caseNumber)
                .map(CaseResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("Case not found: " + caseNumber));
    }

    // Public verify — no auth
    public CaseResponse verify(String caseNumber) {
        return findByNumber(caseNumber);
    }

    public List<CaseResponse> findByCitizen(String nationalId) {
        return Stream.concat(
                caseRepo.findByPlaintiffNationalId(nationalId).stream(),
                caseRepo.findByDefendantNationalId(nationalId).stream()
        ).distinct().map(CaseResponse::from).toList();
    }

    public List<CaseResponse> findByCourt(String courtCode, CaseStatus status) {
        if (status != null)
            return caseRepo.findByCourtCodeAndStatus(courtCode, status).stream().map(CaseResponse::from).toList();
        return caseRepo.findByCourtCode(courtCode).stream().map(CaseResponse::from).toList();
    }

    protected String generateCaseNumber() {
        int year = LocalDate.now().getYear();
        seqRepo.insertIfNotExists(year);
        CaseSequence seq = seqRepo.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Sequence missing for year " + year));
        int next = seq.getNextValue();
        seq.setNextValue(next + 1);
        seqRepo.save(seq);
        return "TM-CASE-%d%06d".formatted(year, next);
    }
}
