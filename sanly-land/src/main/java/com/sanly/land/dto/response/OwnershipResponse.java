package com.sanly.land.dto.response;

import com.sanly.land.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record OwnershipResponse(
        UUID ownershipId,
        String cadastralNumber,
        String ownerNationalId,
        int ownershipShare,
        OwnershipType ownershipType,
        LocalDate acquiredAt,
        AcquiredVia acquiredVia,
        OwnershipStatus status,
        LocalDateTime createdAt
) {
    public static OwnershipResponse from(Ownership o) {
        return new OwnershipResponse(o.getOwnershipId(), o.getCadastralNumber(), o.getOwnerNationalId(),
                o.getOwnershipShare(), o.getOwnershipType(), o.getAcquiredAt(),
                o.getAcquiredVia(), o.getStatus(), o.getCreatedAt());
    }
}
