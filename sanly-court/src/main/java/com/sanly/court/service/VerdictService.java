package com.sanly.court.service;

import com.sanly.court.client.BridgePublisherService;
import com.sanly.court.client.NotificationClient;
import com.sanly.court.client.PoliceServiceClient;
import com.sanly.court.config.UserDetailsImpl;
import com.sanly.court.dto.request.IssueVerdictRequest;
import com.sanly.court.dto.response.VerdictResponse;
import com.sanly.court.entity.*;
import com.sanly.court.exception.DuplicateResourceException;
import com.sanly.court.exception.InvalidOperationException;
import com.sanly.court.exception.RecordNotFoundException;
import com.sanly.court.repository.CourtCaseRepository;
import com.sanly.court.repository.CourtFineRepository;
import com.sanly.court.repository.VerdictRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerdictService {

    private final VerdictRepository verdictRepo;
    private final CourtCaseRepository caseRepo;
    private final CourtFineRepository fineRepo;
    private final BridgePublisherService bridgePublisher;
    private final PoliceServiceClient policeClient;
    private final NotificationClient notificationClient;

    @Transactional
    public VerdictResponse issue(String caseNumber, IssueVerdictRequest req) {
        if (verdictRepo.existsByCaseNumber(caseNumber))
            throw new DuplicateResourceException("Verdict already issued for case: " + caseNumber);

        CourtCase courtCase = caseRepo.findByCaseNumberWithLock(caseNumber)
                .orElseThrow(() -> new RecordNotFoundException("Case not found: " + caseNumber));

        if (courtCase.getStatus() == CaseStatus.DECIDED
                || courtCase.getStatus() == CaseStatus.CLOSED
                || courtCase.getStatus() == CaseStatus.DISMISSED) {
            throw new InvalidOperationException("Case is already " + courtCase.getStatus());
        }

        // Enforce: judge can only issue verdicts for their assigned cases
        UUID officerId = getOfficerId();
        if (officerId != null && courtCase.getAssignedJudgeOfficerId() != null
                && !officerId.equals(courtCase.getAssignedJudgeOfficerId())) {
            throw new InvalidOperationException("You are not the assigned judge for case: " + caseNumber);
        }

        LocalDateTime now = LocalDateTime.now();
        Verdict verdict = verdictRepo.save(Verdict.builder()
                .caseNumber(caseNumber)
                .verdictType(req.verdictType())
                .summary(req.summary())
                .issuedByJudgeOfficerId(officerId)
                .issuedAt(now)
                .appealed(false)
                .build());

        // Close the case
        courtCase.setStatus(CaseStatus.DECIDED);
        courtCase.setClosedAt(now);
        caseRepo.save(courtCase);

        // Look up total fines for this case (include in bridge payload)
        String totalFines = fineRepo.findByCaseNumber(caseNumber).stream()
                .map(CourtFine::getAmount)
                .reduce("0", (a, b) -> {
                    try { return String.valueOf(Double.parseDouble(a) + Double.parseDouble(b)); }
                    catch (Exception e) { return a; }
                });

        // Publish to Bridge
        bridgePublisher.publishCourtOrder(courtCase, verdict, totalFines);

        // GUILTY verdict → auto-add criminal record in police
        if (req.verdictType() == VerdictType.GUILTY) {
            policeClient.addCriminalRecord(
                    courtCase.getDefendantNationalId(), caseNumber, req.summary());
        }

        // Notify both parties
        Map<String, String> meta = Map.of(
                "caseNumber", caseNumber,
                "verdictType", req.verdictType().name(),
                "appealDeadline", verdict.getAppealDeadline().toString());
        notificationClient.send(courtCase.getPlaintiffNationalId(), "COURT_VERDICT_ISSUED", "TK", meta);
        notificationClient.send(courtCase.getDefendantNationalId(), "COURT_VERDICT_ISSUED", "TK", meta);

        return VerdictResponse.from(verdict);
    }

    public VerdictResponse findByCaseNumber(String caseNumber) {
        return verdictRepo.findByCaseNumber(caseNumber)
                .map(VerdictResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("No verdict for case: " + caseNumber));
    }

    private UUID getOfficerId() {
        try {
            Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (p instanceof UserDetailsImpl ud) return ud.getOfficerId();
        } catch (Exception ignored) {}
        return null;
    }
}
