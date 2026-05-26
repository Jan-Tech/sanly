package com.sanly.notifications.dto.request;

import com.sanly.notifications.entity.EventType;
import com.sanly.notifications.entity.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SendNotificationRequest {

    @NotBlank(message = "citizenNationalId is required")
    private String citizenNationalId;

    @NotNull(message = "eventType is required")
    private EventType eventType;

    @NotNull(message = "language is required")
    private Language language;

    /** Key-value pairs injected into the template's {variable} placeholders. Values are HTML-sanitized. */
    private Map<String, String> metadata;
}
