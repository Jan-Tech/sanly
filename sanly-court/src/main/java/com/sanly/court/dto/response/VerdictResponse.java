package com.sanly.court.dto.response;

import com.sanly.court.entity.Verdict;
import com.sanly.court.entity.VerdictType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VerdictResponse(
        UUID verdictId, String caseNumber, VerdictType verdictType, String summary,
        UUID issuedByJudgeOfficerId, LocalDateTime issuedAt, LocalDate appealDeadline,
        boolean appealed, LocalDateTime createdAt
) {
    public static VerdictResponse from(Verdict v) {
        return new VerdictResponse(v.getVerdictId(), v.getCaseNumber(), v.getVerdictType(),
                v.getSummary(), v.getIssuedByJudgeOfficerId(), v.getIssuedAt(),
                v.getAppealDeadline(), v.isAppealed(), v.getCreatedAt());
    }
}
