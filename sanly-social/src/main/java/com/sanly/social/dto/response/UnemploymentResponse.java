package com.sanly.social.dto.response;

import com.sanly.social.entity.UnemploymentReason;
import com.sanly.social.entity.UnemploymentRecord;
import com.sanly.social.entity.UnemploymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UnemploymentResponse(
        UUID recordId,
        String citizenNationalId,
        LocalDateTime registeredAt,
        String lastEmployer,
        LocalDate lastEmploymentDate,
        UnemploymentReason reason,
        UnemploymentStatus status,
        LocalDateTime updatedAt
) {
    public static UnemploymentResponse from(UnemploymentRecord r) {
        return new UnemploymentResponse(r.getRecordId(), r.getCitizenNationalId(),
                r.getRegisteredAt(), r.getLastEmployer(), r.getLastEmploymentDate(),
                r.getReason(), r.getStatus(), r.getUpdatedAt());
    }
}
