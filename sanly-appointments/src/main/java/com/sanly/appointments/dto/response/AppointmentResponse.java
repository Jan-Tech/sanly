package com.sanly.appointments.dto.response;

import com.sanly.appointments.entity.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class AppointmentResponse {

    private UUID appointmentId;
    private String appointmentCode;
    private String citizenNationalId;
    private String officeCode;
    private String officeName;
    private UUID serviceTypeId;
    private String serviceName;
    private LocalDate appointmentDate;
    private LocalTime slotTime;
    private AppointmentStatus status;
    private LocalDateTime bookedAt;
    private String notes;
    private boolean reminderSent;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private Long completedByOfficerId;
    private LocalDateTime completedAt;
    private Integer citizenRating;
    private String citizenFeedback;
}
