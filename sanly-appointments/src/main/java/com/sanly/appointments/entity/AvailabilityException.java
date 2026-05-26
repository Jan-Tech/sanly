package com.sanly.appointments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "availability_exceptions",
    indexes = {
        @Index(name = "idx_exc_office_date", columnList = "office_code, exception_date")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exception_id")
    private Long exceptionId;

    @Column(name = "office_code", length = 12, nullable = false)
    private String officeCode;

    @Column(name = "exception_date", nullable = false)
    private LocalDate exceptionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 20, nullable = false)
    @Builder.Default
    private ExceptionReason reason = ExceptionReason.PUBLIC_HOLIDAY;

    @Column(name = "is_closed", nullable = false)
    @Builder.Default
    private boolean isClosed = true;

    @Column(name = "alternate_open_time")
    private LocalTime alternateOpenTime;

    @Column(name = "alternate_close_time")
    private LocalTime alternateCloseTime;
}
