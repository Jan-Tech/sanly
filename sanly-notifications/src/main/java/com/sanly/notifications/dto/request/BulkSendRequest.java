package com.sanly.notifications.dto.request;

import com.sanly.notifications.entity.EventType;
import com.sanly.notifications.entity.Language;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BulkSendRequest {

    @NotEmpty(message = "recipients must not be empty")
    private List<String> citizenNationalIds;

    @NotNull(message = "eventType is required")
    private EventType eventType;

    @NotNull(message = "language is required")
    private Language language;

    private Map<String, String> metadata;
}
