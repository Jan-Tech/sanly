package com.sanly.bridge.dto.request;

import com.sanly.bridge.entity.DataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PermissionCreateRequest {

    @NotBlank(message = "Requesting institution code is required")
    @Pattern(regexp = "^INST_[A-Z0-9_]+$", message = "Invalid institution code format")
    private String requestingCode;

    @NotBlank(message = "Target institution code is required")
    @Pattern(regexp = "^INST_[A-Z0-9_]+$", message = "Invalid institution code format")
    private String targetCode;

    @NotNull(message = "Data type is required")
    private DataType dataType;
}
