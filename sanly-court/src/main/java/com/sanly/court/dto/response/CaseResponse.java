package com.sanly.court.dto.response;

import com.sanly.court.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CaseResponse(
        UUID caseId, String caseNumber, String courtCode, CaseType caseType,
        String plaintiffNationalId, String defendantNationalId, UUID assignedJudgeOfficerId,
        LocalDateTime filedAt, LocalDate hearingDate, LocalDateTime closedAt,
        CaseStatus status, String summary, LocalDateTime createdAt
) {
    public static CaseResponse from(CourtCase c) {
        return new CaseResponse(c.getCaseId(), c.getCaseNumber(), c.getCourtCode(), c.getCaseType(),
                c.getPlaintiffNationalId(), c.getDefendantNationalId(), c.getAssignedJudgeOfficerId(),
                c.getFiledAt(), c.getHearingDate(), c.getClosedAt(), c.getStatus(),
                c.getSummary(), c.getCreatedAt());
    }
}
