package com.sanly.analytics.dto.response;

import com.sanly.analytics.entity.AnomalyTrend;

import java.util.List;

public record AntiCorruptionKpiResponse(
    long totalAnomalyAlerts,
    long openAnomalyAlerts,
    double resolutionRate,
    List<AnomalyTrend> recentTrends,
    List<String> topFlaggedInstitutions
) {}
