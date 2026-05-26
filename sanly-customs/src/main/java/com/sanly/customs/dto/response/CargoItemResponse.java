package com.sanly.customs.dto.response;

import com.sanly.customs.entity.CargoItem;
import com.sanly.customs.entity.CargoUnit;

import java.time.LocalDateTime;
import java.util.UUID;

public record CargoItemResponse(
        UUID itemId, String declarationCode, String itemDescription, String hsCode,
        String quantity, CargoUnit unit, String unitValue, String totalValue,
        String countryOfOrigin, LocalDateTime createdAt
) {
    public static CargoItemResponse from(CargoItem i) {
        return new CargoItemResponse(i.getItemId(), i.getDeclarationCode(), i.getItemDescription(),
                i.getHsCode(), i.getQuantity(), i.getUnit(), i.getUnitValue(), i.getTotalValue(),
                i.getCountryOfOrigin(), i.getCreatedAt());
    }
}
