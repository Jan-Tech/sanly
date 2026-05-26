package com.sanly.banking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ApproveConsentRequest(
        @NotEmpty List<String> approvedScopes,
        @NotBlank String otpCode
) {}
