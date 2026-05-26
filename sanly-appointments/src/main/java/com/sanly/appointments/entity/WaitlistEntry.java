package com.sanly.appointments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "waitlist_entries",
    indexes = {
        @Index(name = "idx_wl_office_status", columnList = "office_code, status")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "waitlist_id")
    private UUID waitlistId;

    @Column(name = "citizen_national_id", length = 11, nullable = false)
    private String citizenNationalId;

    @Column(name = "office_code", length = 12, nullable = false)
    private String officeCode;

    @Column(name = "service_type_id")
    private UUID serviceTypeId;

    @Column(name = "preferred_date_from")
    private LocalDate preferredDateFrom;

    @Column(name = "preferred_date_to")
    private LocalDate preferredDateTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    @Builder.Default
    private WaitlistStatus status = WaitlistStatus.WAITING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;
}
