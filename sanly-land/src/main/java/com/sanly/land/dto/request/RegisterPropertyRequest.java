package com.sanly.land.dto.request;

import com.sanly.land.entity.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RegisterPropertyRequest(
        @NotNull PropertyType propertyType,
        @NotBlank String address,
        @NotBlank String region,
        @Positive BigDecimal area,
        String description
) {}
