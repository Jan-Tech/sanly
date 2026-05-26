package com.sanly.banking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ConsentRequestDto(
        @NotBlank String citizenNationalId,
        @NotEmpty List<String> requestedScopes,
        @NotBlank String purpose
) {}
