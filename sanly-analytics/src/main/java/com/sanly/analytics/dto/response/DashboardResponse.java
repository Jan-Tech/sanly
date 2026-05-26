package com.sanly.analytics.dto.response;

import com.sanly.analytics.entity.DailySnapshot;
import com.sanly.analytics.entity.RegionalStat;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
    DailySnapshot today,
    Map<String, List<TrendPoint>> trends,
    List<RegionalStat> topRegions,
    ServiceHealthSummary serviceHealth
) {
    public record ServiceHealthSummary(boolean allHealthy, List<String> degradedServices) {}
}
