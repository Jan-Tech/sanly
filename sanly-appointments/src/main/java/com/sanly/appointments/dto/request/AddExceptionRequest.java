package com.sanly.appointments.dto.request;

import com.sanly.appointments.entity.ExceptionReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AddExceptionRequest {

    @NotBlank
    private String officeCode;

    @NotNull
    private LocalDate exceptionDate;

    @NotNull
    private ExceptionReason reason;

    private boolean isClosed = true;

    private LocalTime alternateOpenTime;
    private LocalTime alternateCloseTime;
}
