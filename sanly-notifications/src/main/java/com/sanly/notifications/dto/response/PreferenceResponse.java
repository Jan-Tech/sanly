package com.sanly.notifications.dto.response;

import com.sanly.notifications.entity.EventType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PreferenceResponse {
    private UUID preferenceId;
    private EventType eventType;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean inAppEnabled;
    private boolean enabled;
}
