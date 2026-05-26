package com.sanly.court.dto.response;

import com.sanly.court.entity.CourtFine;
import com.sanly.court.entity.FineStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FineResponse(
        UUID fineId, String fineCode, String caseNumber, String citizenNationalId,
        String amount, String reason, LocalDateTime issuedAt, LocalDate dueDate,
        FineStatus status, LocalDateTime paidAt, String notes, LocalDateTime createdAt
) {
    public static FineResponse from(CourtFine f) {
        return new FineResponse(f.getFineId(), f.getFineCode(), f.getCaseNumber(), f.getCitizenNationalId(),
                f.getAmount(), f.getReason(), f.getIssuedAt(), f.getDueDate(),
                f.getStatus(), f.getPaidAt(), f.getNotes(), f.getCreatedAt());
    }
}
