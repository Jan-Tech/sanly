package com.sanly.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenProfileResponse {

    private String consentCode;
    private String bankCode;
    private String accessedAt;
    private String tokenExpiresAt;
    private List<String> scopesAccessed;
    private Map<String, Object> data; // keyed by scope category
}
