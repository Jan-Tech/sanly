package com.sanly.customs.dto.request;

import com.sanly.customs.entity.CargoUnit;

public record AddCargoItemRequest(
        String itemDescription,
        String hsCode,
        String quantity,
        CargoUnit unit,
        String unitValue,
        String totalValue,
        String countryOfOrigin
) {}
