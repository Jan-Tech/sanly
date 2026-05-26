package com.sanly.notifications.dto.request;

import com.sanly.notifications.entity.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePreferencesRequest {

    @NotNull
    private EventType eventType;

    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean inAppEnabled;
    private Boolean enabled;
}
