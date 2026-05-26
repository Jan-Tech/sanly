package com.sanly.registry.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionOtpResponse {
    private boolean sent;
    private Boolean valid;
    private String phoneMasked;
    private String message;
}
