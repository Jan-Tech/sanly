package com.sanly.notifications.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTemplateRequest {

    @NotBlank
    private String titleTemplate;

    @NotBlank
    private String bodyTemplate;

    private Boolean isActive;
}
