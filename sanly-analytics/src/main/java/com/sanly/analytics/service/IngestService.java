package com.sanly.analytics.service;

import com.sanly.analytics.dto.request.AnomalyIngestRequest;
import com.sanly.analytics.dto.request.DailyIngestRequest;
import com.sanly.analytics.dto.request.RegionalIngestRequest;
import com.sanly.analytics.dto.request.ServiceUsageIngestRequest;
import com.sanly.analytics.entity.AnomalyTrend;
import com.sanly.analytics.entity.DailySnapshot;
import com.sanly.analytics.entity.RegionalStat;
import com.sanly.analytics.entity.ServiceUsageStat;
import com.sanly.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestService {

    private final DailySnapshotRepository snapshotRepo;
    private final ServiceUsageStatRepository usageRepo;
    private final RegionalStatRepository regionalRepo;
    private final AnomalyTrendRepository anomalyRepo;

    @Transactional
    public DailySnapshot ingestDaily(DailyIngestRequest req) {
        LocalDate today = LocalDate.now();
        DailySnapshot snap = snapshotRepo.findBySnapshotDate(today)
                .orElseGet(() -> snapshotRepo.save(DailySnapshot.builder()
                        .snapshotId(UUID.randomUUID())
                        .snapshotDate(today)
                        .build()));

        if (req.totalCitizens() != null)         snap.setTotalCitizens(req.totalCitizens());
        if (req.activeCitizens() != null)         snap.setActiveCitizens(req.activeCitizens());
        if (req.deceasedCitizens() != null)       snap.setDeceasedCitizens(req.deceasedCitizens());
        if (req.newRegistrationsToday() != null)  snap.setNewRegistrationsToday(req.newRegistrationsToday());
        if (req.totalBusinesses() != null)        snap.setTotalBusinesses(req.totalBusinesses());
        if (req.activeBusinesses() != null)       snap.setActiveBusinesses(req.activeBusinesses());
        if (req.newBusinessesToday() != null)     snap.setNewBusinessesToday(req.newBusinessesToday());
        if (req.totalLicenses() != null)          snap.setTotalLicenses(req.totalLicenses());
        if (req.licensesIssuedToday() != null)    snap.setLicensesIssuedToday(req.licensesIssuedToday());
        if (req.totalDiplomas() != null)          snap.setTotalDiplomas(req.totalDiplomas());
        if (req.diplomasIssuedToday() != null)    snap.setDiplomasIssuedToday(req.diplomasIssuedToday());
        if (req.totalProperties() != null)        snap.setTotalProperties(req.totalProperties());
        if (req.transfersToday() != null)         snap.setTransfersToday(req.transfersToday());
        if (req.totalBenefitClaims() != null)     snap.setTotalBenefitClaims(req.totalBenefitClaims());
        if (req.activeClaimants() != null)        snap.setActiveClaimants(req.activeClaimants());
        if (req.totalCourtCases() != null)        snap.setTotalCourtCases(req.totalCourtCases());
        if (req.openCases() != null)              snap.setOpenCases(req.openCases());
        if (req.totalCustomsDeclarations() != null) snap.setTotalCustomsDeclarations(req.totalCustomsDeclarations());
        if (req.clearancesToday() != null)        snap.setClearancesToday(req.clearancesToday());
        if (req.totalAppointments() != null)      snap.setTotalAppointments(req.totalAppointments());
        if (req.appointmentsToday() != null)      snap.setAppointmentsToday(req.appointmentsToday());
        if (req.noShowCount() != null)            snap.setNoShowCount(req.noShowCount());
        if (req.avgAppointmentRating() != null)   snap.setAvgAppointmentRating(req.avgAppointmentRating());
        if (req.totalBridgeExchanges() != null)   snap.setTotalBridgeExchanges(req.totalBridgeExchanges());
        if (req.exchangesToday() != null)         snap.setExchangesToday(req.exchangesToday());
        if (req.totalAnomalyAlerts() != null)     snap.setTotalAnomalyAlerts(req.totalAnomalyAlerts());
        if (req.openAnomalyAlerts() != null)      snap.setOpenAnomalyAlerts(req.openAnomalyAlerts());

        snap.setUpdatedAt(LocalDateTime.now());
        DailySnapshot saved = snapshotRepo.save(snap);
        log.debug("Ingest daily from {}: snapshot updated for {}", req.serviceName(), today);
        return saved;
    }

    @Transactional
    public ServiceUsageStat ingestServiceUsage(ServiceUsageIngestRequest req) {
        LocalDate today = LocalDate.now();
        String endpoint = req.endpoint() != null ? req.endpoint() : "TOTAL";
        ServiceUsageStat stat = usageRepo.findByStatDateAndServiceNameAndEndpoint(today, req.serviceName(), endpoint)
                .orElseGet(() -> usageRepo.save(ServiceUsageStat.builder()
                        .statId(UUID.randomUUID())
                        .statDate(today)
                        .serviceName(req.serviceName())
                        .endpoint(endpoint)
                        .build()));
        stat.setRequestCount(stat.getRequestCount() + req.requestCount());
        stat.setErrorCount(stat.getErrorCount() + req.errorCount());
        stat.setAvgResponseMs(req.avgResponseMs() > 0 ? req.avgResponseMs() : stat.getAvgResponseMs());
        long total = stat.getRequestCount();
        stat.setErrorRate(total > 0 ? (double) stat.getErrorCount() / total * 100.0 : 0.0);
        return usageRepo.save(stat);
    }

    @Transactional
    public RegionalStat ingestRegional(RegionalIngestRequest req) {
        LocalDate today = LocalDate.now();
        RegionalStat stat = regionalRepo.findByStatDateAndRegion(today, req.region())
                .orElseGet(() -> regionalRepo.save(RegionalStat.builder()
                        .statId(UUID.randomUUID())
                        .statDate(today)
                        .region(req.region())
                        .build()));
        if (req.citizenCount() != null)      stat.setCitizenCount(req.citizenCount());
        if (req.businessCount() != null)     stat.setBusinessCount(req.businessCount());
        if (req.propertyCount() != null)     stat.setPropertyCount(req.propertyCount());
        if (req.appointmentCount() != null)  stat.setAppointmentCount(req.appointmentCount());
        if (req.benefitClaimCount() != null) stat.setBenefitClaimCount(req.benefitClaimCount());
        return regionalRepo.save(stat);
    }

    @Transactional
    public AnomalyTrend ingestAnomaly(AnomalyIngestRequest req) {
        LocalDate today = LocalDate.now();
        AnomalyTrend trend = anomalyRepo.findByStatDateAndInstitutionCodeAndAlertType(today, req.institutionCode(), req.alertType())
                .orElseGet(() -> anomalyRepo.save(AnomalyTrend.builder()
                        .trendId(UUID.randomUUID())
                        .statDate(today)
                        .institutionCode(req.institutionCode())
                        .alertType(req.alertType())
                        .build()));
        trend.setAlertCount(req.alertCount());
        trend.setResolvedCount(req.resolvedCount());
        trend.setAvgResolutionHours(req.avgResolutionHours());
        return anomalyRepo.save(trend);
    }
}
