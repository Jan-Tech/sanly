package com.sanly.tax.dto.request;

import com.sanly.tax.entity.TaxpayerType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TaxpayerRegisterRequest {
    @NotBlank @Size(min = 5, max = 30) private String citizenNationalId;
    @NotNull private TaxpayerType taxpayerType;
    /** Optional: LOW, MEDIUM, HIGH — stored encrypted */
    private String annualIncomeClass;
}
