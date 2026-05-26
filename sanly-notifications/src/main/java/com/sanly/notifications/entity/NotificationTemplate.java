package com.sanly.notifications.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "notification_templates",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_template_event_lang_channel",
        columnNames = {"event_type", "language", "channel"}))
@Data
@NoArgsConstructor
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "template_id", updatable = false, nullable = false)
    private UUID templateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 60, nullable = false)
    private EventType eventType;

    @Column(name = "title_template", length = 200, nullable = false)
    private String titleTemplate;

    @Column(name = "body_template", columnDefinition = "TEXT", nullable = false)
    private String bodyTemplate;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20, nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 5, nullable = false)
    private Language language;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
