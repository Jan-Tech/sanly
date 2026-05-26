package com.sanly.registry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhoneUpdateRequest {
    /** E.164 format — e.g. +99361234567 */
    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "Phone must be in E.164 format, e.g. +99361234567")
    private String phoneNumber;

    /** OTP code sent to the new phone number for verification. */
    private String otpCode;
}
