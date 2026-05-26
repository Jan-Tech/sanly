package com.sanly.land.dto.response;

import com.sanly.land.entity.PropertyValuation;
import com.sanly.land.entity.ValuationPurpose;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ValuationResponse(
        UUID valuationId,
        String cadastralNumber,
        String valuationAmount,
        LocalDate valuationDate,
        UUID valuedByOfficerId,
        ValuationPurpose purpose,
        LocalDateTime createdAt
) {
    public static ValuationResponse from(PropertyValuation v) {
        return new ValuationResponse(v.getValuationId(), v.getCadastralNumber(),
                v.getValuationAmount(), v.getValuationDate(), v.getValuedByOfficerId(),
                v.getPurpose(), v.getCreatedAt());
    }
}
