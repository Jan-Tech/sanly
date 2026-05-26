package com.sanly.registry.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    /** Optional — preferred language for OTP SMS (EN / TK / RU). Defaults to EN. */
    private String language;
}
