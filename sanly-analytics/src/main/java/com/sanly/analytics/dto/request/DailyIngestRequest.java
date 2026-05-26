package com.sanly.analytics.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DailyIngestRequest(
    @NotBlank String serviceName,
    Long totalCitizens,
    Long activeCitizens,
    Long deceasedCitizens,
    Long newRegistrationsToday,
    Long totalBusinesses,
    Long activeBusinesses,
    Long newBusinessesToday,
    Long totalLicenses,
    Long licensesIssuedToday,
    Long totalDiplomas,
    Long diplomasIssuedToday,
    Long totalProperties,
    Long transfersToday,
    Long totalBenefitClaims,
    Long activeClaimants,
    Long totalCourtCases,
    Long openCases,
    Long totalCustomsDeclarations,
    Long clearancesToday,
    Long totalAppointments,
    Long appointmentsToday,
    Long noShowCount,
    Double avgAppointmentRating,
    Long totalBridgeExchanges,
    Long exchangesToday,
    Long totalAnomalyAlerts,
    Long openAnomalyAlerts
) {}
