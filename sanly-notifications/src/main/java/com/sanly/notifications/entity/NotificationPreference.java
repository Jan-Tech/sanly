package com.sanly.notifications.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "notification_preferences",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_pref_nin_event",
        columnNames = {"citizen_national_id", "event_type"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "preference_id", updatable = false, nullable = false)
    private UUID preferenceId;

    @Column(name = "citizen_national_id", nullable = false, length = 30)
    private String citizenNationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 60, nullable = false)
    private EventType eventType;

    @Builder.Default
    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Builder.Default
    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled = false;

    @Builder.Default
    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled = true;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
