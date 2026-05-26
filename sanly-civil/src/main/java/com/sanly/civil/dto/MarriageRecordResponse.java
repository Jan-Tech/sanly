package com.sanly.civil.dto;

import com.sanly.civil.entity.MarriageRecord;
import com.sanly.civil.entity.MarriageStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MarriageRecordResponse(UUID id, String certificateNumber,
                                      String spouse1NationalId, String spouse1FullName,
                                      String spouse2NationalId, String spouse2FullName,
                                      LocalDate marriageDate, MarriageStatus status,
                                      LocalDate dissolutionDate, Instant createdAt) {
    public static MarriageRecordResponse from(MarriageRecord r) {
        return new MarriageRecordResponse(r.getId(), r.getCertificateNumber(),
                r.getSpouse1NationalId(), r.getSpouse1FullName(),
                r.getSpouse2NationalId(), r.getSpouse2FullName(),
                r.getMarriageDate(), r.getStatus(), r.getDissolutionDate(), r.getCreatedAt());
    }
}
