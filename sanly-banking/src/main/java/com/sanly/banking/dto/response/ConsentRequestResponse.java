package com.sanly.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequestResponse {

    private String consentCode;
    private String bankCode;
    private String bankName;
    private String status;
    private String requestedAt;
    private String expiresAt;
    private List<ScopeDescription> requestedScopes;
    private String purpose; // decrypted for citizen view
}
