package com.sanly.analytics.service;

import com.sanly.analytics.dto.response.DashboardResponse;
import com.sanly.analytics.dto.response.TrendPoint;
import com.sanly.analytics.entity.DailySnapshot;
import com.sanly.analytics.entity.RegionalStat;
import com.sanly.analytics.repository.DailySnapshotRepository;
import com.sanly.analytics.repository.RegionalStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final DailySnapshotRepository snapshotRepo;
    private final RegionalStatRepository regionalRepo;

    public DailySnapshot getOrCreateToday() {
        return snapshotRepo.findBySnapshotDate(LocalDate.now())
                .orElseGet(() -> snapshotRepo.save(DailySnapshot.builder()
                        .snapshotId(UUID.randomUUID())
                        .snapshotDate(LocalDate.now())
                        .build()));
    }

    public DashboardResponse getDashboard() {
        DailySnapshot today = getOrCreateToday();
        LocalDate from = LocalDate.now().minusDays(7);
        List<DailySnapshot> week = snapshotRepo.findBySnapshotDateBetweenOrderBySnapshotDateAsc(from, LocalDate.now());

        List<TrendPoint> citizenTrend = week.stream()
                .map(s -> new TrendPoint(s.getSnapshotDate(), s.getNewRegistrationsToday())).collect(Collectors.toList());
        List<TrendPoint> businessTrend = week.stream()
                .map(s -> new TrendPoint(s.getSnapshotDate(), s.getNewBusinessesToday())).collect(Collectors.toList());
        List<TrendPoint> exchangeTrend = week.stream()
                .map(s -> new TrendPoint(s.getSnapshotDate(), s.getExchangesToday())).collect(Collectors.toList());
        List<TrendPoint> anomalyTrend = week.stream()
                .map(s -> new TrendPoint(s.getSnapshotDate(), s.getOpenAnomalyAlerts())).collect(Collectors.toList());

        List<RegionalStat> topRegions = regionalRepo.findLatestAll().stream().limit(5).collect(Collectors.toList());

        Map<String, List<TrendPoint>> trends = Map.of(
                "citizens", citizenTrend,
                "businesses", businessTrend,
                "exchanges", exchangeTrend,
                "anomalies", anomalyTrend
        );

        DashboardResponse.ServiceHealthSummary health = new DashboardResponse.ServiceHealthSummary(true, List.of());
        return new DashboardResponse(today, trends, topRegions, health);
    }

    public List<DailySnapshot> getHistorical(LocalDate from, LocalDate to) {
        return snapshotRepo.findBySnapshotDateBetweenOrderBySnapshotDateAsc(from, to);
    }

    public DailySnapshot getByDate(LocalDate date) {
        return snapshotRepo.findBySnapshotDate(date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No snapshot for " + date));
    }

    public List<TrendPoint> getTrend(String metric, int days) {
        LocalDate from = LocalDate.now().minusDays(days);
        List<DailySnapshot> snapshots = snapshotRepo.findBySnapshotDateBetweenOrderBySnapshotDateAsc(from, LocalDate.now());
        return snapshots.stream().map(s -> new TrendPoint(s.getSnapshotDate(), extractMetric(s, metric))).collect(Collectors.toList());
    }

    private double extractMetric(DailySnapshot s, String metric) {
        return switch (metric.toLowerCase()) {
            case "citizens", "new_citizens" -> s.getNewRegistrationsToday();
            case "total_citizens" -> s.getTotalCitizens();
            case "businesses", "new_businesses" -> s.getNewBusinessesToday();
            case "total_businesses" -> s.getTotalBusinesses();
            case "exchanges" -> s.getExchangesToday();
            case "anomalies" -> s.getOpenAnomalyAlerts();
            case "appointments" -> s.getAppointmentsToday();
            case "licenses" -> s.getLicensesIssuedToday();
            case "diplomas" -> s.getDiplomasIssuedToday();
            case "clearances" -> s.getClearancesToday();
            case "rating" -> s.getAvgAppointmentRating();
            default -> 0;
        };
    }
}
