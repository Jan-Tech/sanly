package com.sanly.bridge.service;

import com.sanly.bridge.client.NotificationClient;
import com.sanly.bridge.entity.*;
import com.sanly.bridge.repository.AnomalyAlertRepository;
import com.sanly.bridge.repository.ExchangeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Runs after every exchange log save. Detects suspicious patterns that may indicate
 * "favor culture" access — quiet data pulls for personal purposes with no accountability.
 *
 * All detection methods run @Async (non-blocking) and @Transactional (own connection).
 * Each method deduplicates before creating an alert — no duplicate OPEN alerts for
 * the same institution + alert type within 24 hours.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final ExchangeLogRepository  exchangeLogRepository;
    private final AnomalyAlertRepository anomalyAlertRepository;
    private final NotificationClient     notificationClient;

    private static final Set<DataType> SENSITIVE_TYPES =
            Set.of(DataType.CRIMINAL_RECORD, DataType.MEDICAL_CLEARANCE);

    @Async
    @Transactional
    public void analyze(ExchangeLog entry) {
        if (entry.getOperationType() != OperationType.QUERY) return;
        try {
            checkHighVolumeQueries(entry);
            checkRepeatedCitizenQuery(entry);
            checkOffHoursSensitiveAccess(entry);
            checkSelfQuerySuspicion(entry);
            checkBulkCitizenScan(entry);
            checkDeniedRepeatedAttempt(entry);
        } catch (Exception ex) {
            log.error("Anomaly detection error for exchange {}: {}", entry.getId(), ex.getMessage(), ex);
        }
    }

    // ── Rule 1: High-volume queries ───────────────────────────────────────────

    private void checkHighVolumeQueries(ExchangeLog entry) {
        long count = exchangeLogRepository.countQueryByInstitutionSince(
                entry.getRequestingCode(), LocalDateTime.now().minusHours(1));
        if (count > 20) {
            createAlertIfNoDuplicate(
                    entry.getRequestingCode(),
                    AnomalyType.HIGH_VOLUME_QUERIES,
                    AlertSeverity.MEDIUM,
                    String.format("Institution %s made %d queries in the last hour (threshold: 20)",
                            entry.getRequestingCode(), count),
                    null);
        }
    }

    // ── Rule 2: Repeated citizen query ────────────────────────────────────────

    private void checkRepeatedCitizenQuery(ExchangeLog entry) {
        if (!StringUtils.hasText(entry.getNationalId())) return;
        long count = exchangeLogRepository.countQueryByInstitutionAndNationalIdSince(
                entry.getRequestingCode(), entry.getNationalId(), LocalDateTime.now().minusDays(7));
        if (count > 3) {
            createAlertIfNoDuplicate(
                    entry.getRequestingCode(),
                    AnomalyType.REPEATED_CITIZEN_QUERY,
                    AlertSeverity.HIGH,
                    String.format("Institution %s queried citizen %s %d times in 7 days (threshold: 3)",
                            entry.getRequestingCode(), entry.getNationalId(), count),
                    entry.getNationalId());
        }
    }

    // ── Rule 3: Off-hours sensitive access ────────────────────────────────────

    private void checkOffHoursSensitiveAccess(ExchangeLog entry) {
        if (!SENSITIVE_TYPES.contains(entry.getDataType())) return;
        int hour = entry.getExchangedAt().getHour();
        if (hour >= 22 || hour < 6) {
            createAlertIfNoDuplicate(
                    entry.getRequestingCode(),
                    AnomalyType.OFF_HOURS_SENSITIVE_ACCESS,
                    AlertSeverity.HIGH,
                    String.format("Institution %s accessed sensitive data type %s at %02d:00 (off-hours: 22:00–06:00)",
                            entry.getRequestingCode(), entry.getDataType(), hour),
                    entry.getNationalId());
        }
    }

    // ── Rule 4: Unaccompanied ROUTINE_CHECK on sensitive data ─────────────────

    private void checkSelfQuerySuspicion(ExchangeLog entry) {
        if (!SENSITIVE_TYPES.contains(entry.getDataType())) return;
        if (entry.getPurposeCode() != QueryPurpose.ROUTINE_CHECK) return;
        if (StringUtils.hasText(entry.getCaseReference())) return;
        createAlertIfNoDuplicate(
                entry.getRequestingCode(),
                AnomalyType.SELF_QUERY_SUSPICION,
                AlertSeverity.MEDIUM,
                String.format("Institution %s queried sensitive data type %s with purpose ROUTINE_CHECK "
                        + "and no case reference — possible favor access",
                        entry.getRequestingCode(), entry.getDataType()),
                entry.getNationalId());
    }

    // ── Rule 5: Bulk citizen scan ─────────────────────────────────────────────

    private void checkBulkCitizenScan(ExchangeLog entry) {
        long distinctCitizens = exchangeLogRepository.countDistinctNationalIdsByInstitutionSince(
                entry.getRequestingCode(), LocalDateTime.now().minusHours(1));
        if (distinctCitizens > 50) {
            createAlertIfNoDuplicate(
                    entry.getRequestingCode(),
                    AnomalyType.BULK_CITIZEN_SCAN,
                    AlertSeverity.CRITICAL,
                    String.format("Institution %s queried %d distinct citizens in the last hour (threshold: 50)",
                            entry.getRequestingCode(), distinctCitizens),
                    null);
        }
    }

    // ── Rule 6: Repeated denied attempts (permission probing) ─────────────────

    private void checkDeniedRepeatedAttempt(ExchangeLog entry) {
        if (entry.getResult() != ExchangeResult.DENIED) return;
        long deniedCount = exchangeLogRepository.countDeniedByInstitutionSince(
                entry.getRequestingCode(), LocalDateTime.now().minusHours(1));
        if (deniedCount > 5) {
            createAlertIfNoDuplicate(
                    entry.getRequestingCode(),
                    AnomalyType.DENIED_REPEATED_ATTEMPT,
                    AlertSeverity.MEDIUM,
                    String.format("Institution %s received %d denied responses in the last hour (threshold: 5) "
                            + "— possible permission probing",
                            entry.getRequestingCode(), deniedCount),
                    null);
        }
    }

    // ── Deduplication + persistence ───────────────────────────────────────────

    private void createAlertIfNoDuplicate(String institutionCode, AnomalyType alertType,
                                           AlertSeverity severity, String description,
                                           String nationalId) {
        boolean duplicate = anomalyAlertRepository
                .existsByInstitutionCodeAndAlertTypeAndStatusAndDetectedAtAfter(
                        institutionCode, alertType, AlertStatus.OPEN,
                        LocalDateTime.now().minusHours(24));
        if (duplicate) return;

        AnomalyAlert alert = AnomalyAlert.builder()
                .institutionCode(institutionCode)
                .alertType(alertType)
                .description(description)
                .severity(severity)
                .nationalIdInvolved(nationalId)
                .detectedAt(LocalDateTime.now())
                .status(AlertStatus.OPEN)
                .build();
        anomalyAlertRepository.save(alert);

        log.warn("ANOMALY [{}] severity={} institution={}: {}",
                alertType, severity, institutionCode, description);

        // Notify the citizen whose data triggered the anomaly
        if (StringUtils.hasText(nationalId)) {
            notificationClient.send(nationalId, "ANOMALY_DETECTED_ON_YOUR_DATA", "EN",
                    Map.of(
                            "institutionName", institutionCode,
                            "detectedAt",      LocalDateTime.now().toString()
                    ));
        }
    }
}
