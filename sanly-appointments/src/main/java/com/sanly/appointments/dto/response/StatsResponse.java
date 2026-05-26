package com.sanly.appointments.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatsResponse {

    private long totalBooked;
    private long totalCompleted;
    private long totalCancelled;
    private long totalNoShow;
    private double completionRate;
    private double noShowRate;
    private double averageRating;
    private String busiestSlot;
}
