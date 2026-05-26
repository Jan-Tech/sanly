package com.sanly.appointments.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class SlotResponse {

    private LocalTime slotTime;
    private boolean available;
    private int spotsRemaining;
}
