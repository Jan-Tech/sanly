package com.sanly.court.dto.request;
import com.sanly.court.entity.CaseType;
public record FileCaseRequest(
        String courtCode,
        CaseType caseType,
        String plaintiffNationalId,
        String defendantNationalId,
        String summary,
        String notes
) {}
