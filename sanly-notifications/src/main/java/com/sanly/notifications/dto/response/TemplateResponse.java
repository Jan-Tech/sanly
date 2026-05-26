package com.sanly.notifications.dto.response;

import com.sanly.notifications.entity.Channel;
import com.sanly.notifications.entity.EventType;
import com.sanly.notifications.entity.Language;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TemplateResponse {
    private UUID templateId;
    private EventType eventType;
    private String titleTemplate;
    private String bodyTemplate;
    private Channel channel;
    private Language language;
    private boolean isActive;
}
