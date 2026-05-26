package com.sanly.land.dto.request;

import com.sanly.land.entity.ValuationPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AddValuationRequest(
        @NotBlank String cadastralNumber,
        @NotBlank String valuationAmount,
        @NotNull LocalDate valuationDate,
        @NotNull ValuationPurpose purpose
) {}
