package com.sanly.notifications.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications",
    indexes = {
        @Index(name = "idx_notif_nin",     columnList = "citizen_national_id"),
        @Index(name = "idx_notif_status",  columnList = "status"),
        @Index(name = "idx_notif_created", columnList = "created_at")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id", updatable = false, nullable = false)
    private UUID notificationId;

    @Column(name = "citizen_national_id", nullable = false, length = 30)
    private String citizenNationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 60, nullable = false)
    private EventType eventType;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT", nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20, nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 5, nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", length = 15, nullable = false)
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /** JSON map of extra context — licenseNumber, institutionName, etc. Sanitized before storage. */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
