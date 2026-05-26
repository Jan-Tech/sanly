package com.sanly.appointments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(
    name = "availability_schedules",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_schedule",
        columnNames = {"office_code", "day_of_week"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilitySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "office_code", length = 12, nullable = false)
    private String officeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 3, nullable = false)
    private ScheduleDay dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "slot_duration_minutes", nullable = false)
    @Builder.Default
    private int slotDurationMinutes = 30;

    @Column(name = "max_concurrent_appointments", nullable = false)
    @Builder.Default
    private int maxConcurrentAppointments = 3;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
