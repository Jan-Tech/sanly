package com.sanly.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScopeDescription {

    private String scope;       // enum name, e.g. "IDENTITY_FULL"
    private String name;        // human-readable, e.g. "Full Identity"
    private String description; // e.g. "Your full name, date of birth, and residential address"
}
