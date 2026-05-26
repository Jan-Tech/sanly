package com.sanly.notifications.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class NotificationStatsResponse {
    private long totalSentToday;
    private long totalFailedToday;
    private long totalUnread;
    /** eventType → count for the last 24 h */
    private Map<String, Long> byEventType;
    /** channel → count for the last 24 h */
    private Map<String, Long> byChannel;
}
