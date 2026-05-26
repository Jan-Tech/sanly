package com.sanly.documents.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tracked_items",
        indexes = {
            @Index(name = "idx_track_national_id", columnList = "citizen_national_id"),
            @Index(name = "idx_track_completed",   columnList = "is_completed"),
            @Index(name = "idx_track_source",      columnList = "source_item_code")
        })
@Getter
@Setter
public class TrackedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tracking_id", updatable = false, nullable = false)
    private UUID trackingId;

    @Column(name = "tracking_code", length = 22, unique = true, nullable = false)
    private String trackingCode;

    @Column(name = "citizen_national_id", length = 11, nullable = false)
    private String citizenNationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 30, nullable = false)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_service", length = 20, nullable = false)
    private SourceService sourceService;

    @Column(name = "source_item_code", length = 50, unique = true, nullable = false)
    private String sourceItemCode;

    @Column(name = "title", length = 300)
    private String title;

    @Column(name = "current_status", length = 50)
    private String currentStatus;

    @Column(name = "status_description", length = 1000)
    private String statusDescription;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;
}
