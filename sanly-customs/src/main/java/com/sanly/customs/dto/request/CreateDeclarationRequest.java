package com.sanly.customs.dto.request;

import com.sanly.customs.entity.CargoUnit;
import com.sanly.customs.entity.DeclarantType;
import com.sanly.customs.entity.DeclarationType;

public record CreateDeclarationRequest(
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
        String notes
) {}
