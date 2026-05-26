package com.sanly.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredBankResponse {

    private String bankCode;
    private String bankName;
    private String licenseNumber;
    private String contactEmail;
    private String status;
    private String registeredAt;
    private String approvedAt;
}
