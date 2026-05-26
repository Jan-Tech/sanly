package com.sanly.tax.dto.response;

import com.sanly.tax.entity.TaxpayerStatus;
import com.sanly.tax.entity.TaxpayerType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class TaxpayerResponse {
    private UUID recordId;
    private String citizenNationalId, taxId;
    private LocalDate registrationDate;
    private TaxpayerType taxpayerType;
    private TaxpayerStatus status;
    private String annualIncomeClass;
    private LocalDateTime createdAt;
}
