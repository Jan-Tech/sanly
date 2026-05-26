package com.sanly.dmv.dto.request;

import com.sanly.dmv.entity.LicenseStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LicenseStatusRequest {
    @NotNull(message = "Status is required")
    private LicenseStatus status;
    @Size(max = 500)
    private String reason;
}
