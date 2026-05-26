package com.sanly.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentStatusResponse {

    private String consentCode;
    private String status;
    private String consentToken; // only set when APPROVED; cleared after first retrieval
}
