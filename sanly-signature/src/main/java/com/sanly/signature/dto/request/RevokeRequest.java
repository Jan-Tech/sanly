package com.sanly.signature.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RevokeRequest {

    @NotBlank(message = "Revocation reason must not be blank")
    private String reason;
}
