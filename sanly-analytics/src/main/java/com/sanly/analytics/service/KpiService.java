package com.sanly.analytics.service;

import com.sanly.analytics.dto.response.*;
import com.sanly.analytics.entity.AnomalyTrend;
import com.sanly.analytics.entity.DailySnapshot;
import com.sanly.analytics.entity.RegionalStat;
import com.sanly.analytics.entity.ServiceUsageStat;
import com.sanly.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KpiService {

    private final DailySnapshotRepository snapshotRepo;
    private final RegionalStatRepository regionalRepo;
    private final ServiceUsageStatRepository usageRepo;
    private final AnomalyTrendRepository anomalyRepo;

    public PopulationKpiResponse getPopulationKpi() {
        DailySnapshot today = snapshotRepo.findBySnapshotDate(LocalDate.now())
                .orElseGet(() -> DailySnapshot.builder().build());
        DailySnapshot thirtyDaysAgo = snapshotRepo.findBySnapshotDate(LocalDate.now().minusDays(30))
                .orElse(null);

        double growthRate = 0.0;
        if (thirtyDaysAgo != null && thirtyDaysAgo.getTotalCitizens() > 0) {
            growthRate = ((double)(today.getTotalCitizens() - thirtyDaysAgo.getTotalCitizens())
                    / thirtyDaysAgo.getTotalCitizens()) * 100.0;
        }

        List<RegionalStat> topRegions = regionalRepo.findLatestAll().stream().limit(10).collect(Collectors.toList());

        // Gender ratio is approximated at 50/50 since TM-NIN structure-based computation
        // would require querying citizen data — not stored in analytics (privacy by design).
        return new PopulationKpiResponse(
                today.getTotalCitizens(),
                today.getActiveCitizens(),
                today.getDeceasedCitizens(),
                Math.round(growthRate * 100.0) / 100.0,
                50.0,
                50.0,
                topRegions
        );
    }

    public EconomyKpiResponse getEconomyKpi() {
        LocalDate from30 = LocalDate.now().minusDays(30);
        List<DailySnapshot> last30 = snapshotRepo.findBySnapshotDateBetweenOrderBySnapshotDateAsc(from30, LocalDate.now());
        DailySnapshot today = snapshotRepo.findBySnapshotDate(LocalDate.now())
                .orElseGet(() -> DailySnapshot.builder().build());

        long newBusinesses30d = last30.stream().mapToLong(DailySnapshot::getNewBusinessesToday).sum();
        long clearances30d = last30.stream().mapToLong(DailySnapshot::getClearancesToday).sum();
        long transfers30d = last30.stream().mapToLong(DailySnapshot::getTransfersToday).sum();
        double formationRate = last30.isEmpty() ? 0.0 : (double) newBusinesses30d / last30.size();

        return new EconomyKpiResponse(
                Math.round(formationRate * 100.0) / 100.0,
                today.getActiveBusinesses(),
                newBusinesses30d,
                transfers30d,
                clearances30d,
                today.getActiveClaimants(),
                today.getTotalBenefitClaims()
        );
    }

    public ServicesKpiResponse getServicesKpi() {
        DailySnapshot today = snapshotRepo.findBySnapshotDate(LocalDate.now())
                .orElseGet(() -> DailySnapshot.builder().build());
        List<ServiceUsageStat> topServices = usageRepo.findLatestTopServices().stream()
                .limit(8).collect(Collectors.toList());
        long appointments = today.getAppointmentsToday();
        long noShows = today.getNoShowCount();
        double noShowRate = appointments > 0 ? (double) noShows / appointments * 100.0 : 0.0;

        return new ServicesKpiResponse(
                topServices,
                today.getAvgAppointmentRating(),
                Math.round(noShowRate * 100.0) / 100.0,
                today.getAppointmentsToday(),
                today.getTotalAppointments()
        );
    }

    public AntiCorruptionKpiResponse getAntiCorruptionKpi() {
        DailySnapshot today = snapshotRepo.findBySnapshotDate(LocalDate.now())
                .orElseGet(() -> DailySnapshot.builder().build());
        LocalDate from30 = LocalDate.now().minusDays(30);
        List<AnomalyTrend> recentTrends = anomalyRepo.findTopAlertsByDate(from30).stream()
                .limit(20).collect(Collectors.toList());

        long totalAlerts = recentTrends.stream().mapToLong(AnomalyTrend::getAlertCount).sum();
        long resolved = recentTrends.stream().mapToLong(AnomalyTrend::getResolvedCount).sum();
        double resolutionRate = totalAlerts > 0 ? (double) resolved / totalAlerts * 100.0 : 0.0;

        List<String> topFlagged = recentTrends.stream()
                .collect(Collectors.groupingBy(AnomalyTrend::getInstitutionCode,
                        Collectors.summingLong(AnomalyTrend::getAlertCount)))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(e -> e.getKey() + " (" + e.getValue() + " alerts)")
                .collect(Collectors.toList());

        return new AntiCorruptionKpiResponse(
                today.getTotalAnomalyAlerts(),
                today.getOpenAnomalyAlerts(),
                Math.round(resolutionRate * 100.0) / 100.0,
                recentTrends,
                topFlagged
        );
    }
}
