package com.sanly.registry.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendOtpRequest {
    @NotBlank
    private String sessionToken;

    private String language;
}
