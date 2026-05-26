package com.sanly.land.dto.response;

import com.sanly.land.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferApplicationResponse(
        UUID applicationId,
        String cadastralNumber,
        String fromNationalId,
        String toNationalId,
        TransferType transferType,
        String agreedPrice,
        LocalDate applicationDate,
        TransferStatus status,
        UUID processedByOfficerId,
        LocalDateTime processedAt,
        String rejectionReason,
        String notes,
        LocalDateTime createdAt
) {
    public static TransferApplicationResponse from(TransferApplication t) {
        return new TransferApplicationResponse(t.getApplicationId(), t.getCadastralNumber(),
                t.getFromNationalId(), t.getToNationalId(), t.getTransferType(),
                t.getAgreedPrice(), t.getApplicationDate(), t.getStatus(),
                t.getProcessedByOfficerId(), t.getProcessedAt(), t.getRejectionReason(),
                t.getNotes(), t.getCreatedAt());
    }
}
