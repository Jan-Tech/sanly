package com.sanly.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDataAccessResponse {

    private String bankCode;
    private String bankName;
    private String citizenNationalId;
    private String consentCode;
    private String scopesAccessed;
    private String accessedAt;
    private String responseStatus;
}
