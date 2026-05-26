package com.sanly.land.dto.response;

import com.sanly.land.entity.Property;
import com.sanly.land.entity.PropertyStatus;
import com.sanly.land.entity.PropertyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PropertyResponse(
        UUID propertyId,
        String cadastralNumber,
        PropertyType propertyType,
        String address,
        String region,
        BigDecimal area,
        String description,
        PropertyStatus status,
        LocalDateTime registeredAt
) {
    public static PropertyResponse from(Property p) {
        return new PropertyResponse(p.getPropertyId(), p.getCadastralNumber(), p.getPropertyType(),
                p.getAddress(), p.getRegion(), p.getArea(), p.getDescription(),
                p.getStatus(), p.getRegisteredAt());
    }
}
