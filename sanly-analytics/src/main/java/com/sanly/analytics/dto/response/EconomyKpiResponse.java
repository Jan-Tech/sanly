package com.sanly.analytics.dto.response;

public record EconomyKpiResponse(
    double businessFormationRate30d,
    long activeBusinesses,
    long newBusinesses30d,
    long propertyTransfers30d,
    long customsClearances30d,
    long activeBenefitClaimants,
    long totalBenefitClaims
) {}
