package com.sanly.documents.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tracking_updates",
        indexes = {
            @Index(name = "idx_tupdate_code", columnList = "tracking_code")
        })
@Getter
@Setter
public class TrackingUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "update_id", updatable = false, nullable = false)
    private UUID updateId;

    @Column(name = "tracking_code", length = 22, nullable = false)
    private String trackingCode;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "description", length = 1000)
    private String description;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by_service", length = 50)
    private String updatedByService;
}
