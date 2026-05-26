package com.sanly.analytics.dto.response;

import com.sanly.analytics.entity.ServiceUsageStat;

import java.util.List;

public record ServicesKpiResponse(
    List<ServiceUsageStat> topServices,
    double avgAppointmentRating,
    double noShowRate,
    long appointmentsToday,
    long totalAppointments
) {}
