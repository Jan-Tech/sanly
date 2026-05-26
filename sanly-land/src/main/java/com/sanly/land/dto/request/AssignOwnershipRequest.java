package com.sanly.land.dto.request;

import com.sanly.land.entity.AcquiredVia;
import com.sanly.land.entity.OwnershipType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AssignOwnershipRequest(
        @NotBlank String cadastralNumber,
        @NotBlank String ownerNationalId,
        @Min(1) @Max(100) int ownershipShare,
        @NotNull OwnershipType ownershipType,
        @NotNull LocalDate acquiredAt,
        @NotNull AcquiredVia acquiredVia
) {}
