package com.sanly.banking.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateBankStatusRequest(
        @NotBlank String status
) {}
