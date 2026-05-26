package com.sanly.land.dto.request;

import com.sanly.land.entity.TransferType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SubmitTransferRequest(
        @NotBlank String cadastralNumber,
        @NotBlank String fromNationalId,
        @NotBlank String toNationalId,
        @NotNull TransferType transferType,
        String agreedPrice,
        @NotNull LocalDate applicationDate,
        String notes
) {}
