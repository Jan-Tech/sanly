package com.sanly.customs.dto.response;

import com.sanly.customs.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeclarationResponse(
        UUID declarationId,
        String declarationCode,
        DeclarantType declarantType,
        String declarantNationalId,
        String declarantBusinessNumber,
        DeclarationType declarationType,
        String portCode,
        String cargoDescription,
        String hsCode,
        String countryOfOrigin,
        String countryOfDestination,
        String quantity,
        CargoUnit unit,
        String declaredValue,
        String currency,
        String dutiesOwed,
        String dutiesPaid,
        LocalDate declarationDate,
        DeclarationStatus status,
        String rejectionReason,
        LocalDateTime processedAt,
        LocalDateTime createdAt
) {
    public static DeclarationResponse from(CustomsDeclaration d) {
        return new DeclarationResponse(
                d.getDeclarationId(), d.getDeclarationCode(), d.getDeclarantType(),
                d.getDeclarantNationalId(), d.getDeclarantBusinessNumber(), d.getDeclarationType(),
                d.getPortCode(), d.getCargoDescription(), d.getHsCode(),
                d.getCountryOfOrigin(), d.getCountryOfDestination(), d.getQuantity(), d.getUnit(),
                d.getDeclaredValue(), d.getCurrency(), d.getDutiesOwed(), d.getDutiesPaid(),
                d.getDeclarationDate(), d.getStatus(), d.getRejectionReason(),
                d.getProcessedAt(), d.getCreatedAt());
    }
}
