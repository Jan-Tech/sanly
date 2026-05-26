package com.sanly.banking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterBankRequest(
        @NotBlank String bankName,
        @NotBlank String licenseNumber,
        @Email @NotBlank String contactEmail
) {}
