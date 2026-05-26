package com.sanly.police.dto.request;

import com.sanly.police.entity.CheckType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CitizenCheckRequest {
    @NotBlank @Size(min = 5, max = 30) private String citizenNationalId;
    @NotNull private CheckType checkType;
}
