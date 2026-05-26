package com.sanly.notifications.dto.response;

import com.sanly.notifications.entity.Channel;
import com.sanly.notifications.entity.EventType;
import com.sanly.notifications.entity.Language;
import com.sanly.notifications.entity.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID notificationId;
    private String citizenNationalId;
    private EventType eventType;
    private String title;
    private String body;
    private Channel channel;
    private Language language;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private LocalDateTime deliveredAt;
}
