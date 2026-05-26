package com.sanly.analytics.dto.response;

import com.sanly.analytics.entity.RegionalStat;

import java.util.List;

public record PopulationKpiResponse(
    long totalCitizens,
    long activeCitizens,
    long deceasedCitizens,
    double growthRate30d,
    double malePercent,
    double femalePercent,
    List<RegionalStat> topRegions
) {}
