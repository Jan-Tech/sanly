package com.sanly.appointments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
    name = "appointments",
    indexes = {
        @Index(name = "idx_apt_citizen", columnList = "citizen_national_id"),
        @Index(name = "idx_apt_office", columnList = "office_code"),
        @Index(name = "idx_apt_date", columnList = "appointment_date"),
        @Index(name = "idx_apt_status", columnList = "status"),
        @Index(name = "idx_apt_office_date", columnList = "office_code, appointment_date")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "appointment_code", length = 22, unique = true, nullable = false)
    private String appointmentCode;

    @Column(name = "citizen_national_id", length = 11, nullable = false)
    private String citizenNationalId;

    @Column(name = "office_code", length = 12, nullable = false)
    private String officeCode;

    @Column(name = "service_type_id")
    private UUID serviceTypeId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    @CreationTimestamp
    @Column(name = "booked_at", nullable = false, updatable = false)
    private LocalDateTime bookedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reminder_sent", nullable = false)
    @Builder.Default
    private boolean reminderSent = false;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "completed_by_officer_id")
    private Long completedByOfficerId;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "citizen_rating")
    private Integer citizenRating;

    @Column(name = "citizen_feedback", columnDefinition = "TEXT")
    private String citizenFeedback;
}
