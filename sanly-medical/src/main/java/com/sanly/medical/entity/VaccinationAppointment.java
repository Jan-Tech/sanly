package com.sanly.medical.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vaccination_appointments",
    indexes = {
        @Index(name = "idx_va_schedule", columnList = "schedule_id"),
        @Index(name = "idx_va_due_date", columnList = "due_date"),
        @Index(name = "idx_va_status",   columnList = "status")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appointment_id", updatable = false, nullable = false)
    private UUID appointmentId;

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    /** Citizen NIN — denormalized for query convenience */
    @Column(name = "citizen_national_id", nullable = false, length = 11)
    private String citizenNationalId;

    @Column(name = "vaccine_name", nullable = false, length = 100)
    private String vaccineName;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 15)
    private VaccinationStatus status = VaccinationStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completed_by_doctor_id")
    private Long completedByDoctorId;
}
