package com.sanly.bridge.service;

import com.sanly.bridge.client.NotificationClient;
import com.sanly.bridge.entity.*;
import com.sanly.bridge.repository.AnomalyAlertRepository;
import com.sanly.bridge.repository.ExchangeLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnomalyDetectionService.
 *
 * Because the service is injected directly with {@code @InjectMocks} (no Spring proxy),
 * the {@code @Async} annotation is a no-op — {@link AnomalyDetectionService#analyze(ExchangeLog)}
 * runs synchronously in these tests, making assertions straightforward.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnomalyDetectionService")
class AnomalyDetectionServiceTest {

    private static final String INST = "INST_POLICE";
    private static final String NIN  = "50101150010";

    @Mock ExchangeLogRepository  exchangeLogRepository;
    @Mock AnomalyAlertRepository anomalyAlertRepository;
    @Mock NotificationClient     notificationClient;

    @InjectMocks AnomalyDetectionService service;

    @BeforeEach
    void defaultStubs() {
        // No duplicate alerts exist by default
        when(anomalyAlertRepository.existsByInstitutionCodeAndAlertTypeAndStatusAndDetectedAtAfter(
                anyString(), any(), any(), any())).thenReturn(false);
    }

    // ── Rule 1: High-volume queries ───────────────────────────────────────────

    @Test
    @DisplayName("Rule 1: >20 queries in 1 hour creates HIGH_VOLUME_QUERIES MEDIUM alert")
    void analyze_highVolume_createsAlert() {
        when(exchangeLogRepository.countQueryByInstitutionSince(eq(INST), any())).thenReturn(21L);
        // Other rules return benign counts
        stubLowCounters(INST, NIN);

        service.analyze(buildQueryLog(INST, NIN, DataType.TAX_STATUS, ExchangeResult.SUCCESS));

        verifyAlertCreated(AnomalyType.HIGH_VOLUME_QUERIES, AlertSeverity.MEDIUM);
    }

    @Test
    @DisplayName("Rule 1: exactly 20 queries does NOT trigger alert")
    void analyze_highVolume_threshold20_noAlert() {
        when(exchangeLogRepository.countQueryByInstitutionSince(eq(INST), any())).thenReturn(20L);
        stubLowCounters(INST, NIN);

        service.analyze(buildQueryLog(INST, NIN, DataType.TAX_STATUS, ExchangeResult.SUCCESS));

        verify(anomalyAlertRepository, never()).save(argThat(
                a -> a.getAlertType() == AnomalyType.HIGH_VOLUME_QUERIES));
    }

    // ── Rule 2: Repeated citizen query ────────────────────────────────────────

    @Test
    @DisplayName("Rule 2: >3 queries for same citizen in 7 days creates REPEATED_CITIZEN_QUERY HIGH alert")
    void analyze_repeatedCitizenQuery_createsAlert() {
        stubLowCounters(INST, NIN);
        when(exchangeLogRepository.countQueryByInstitutionAndNationalIdSince(
                eq(INST), eq(NIN), any())).thenReturn(4L);

        service.analyze(buildQueryLog(INST, NIN, DataType.TAX_STATUS, ExchangeResult.SUCCESS));

        verifyAlertCreated(AnomalyType.REPEATED_CITIZEN_QUERY, AlertSeverity.HIGH);
    }

    // ── Rule 3: Off-hours sensitive access ────────────────────────────────────

    @Test
    @DisplayName("Rule 3: CRIMINAL_RECORD queried at 23:00 creates OFF_HOURS_SENSITIVE_ACCESS HIGH alert")
    void analyze_offHoursSensitive_createsAlert() {
        stubLowCounters(INST, NIN);
        ExchangeLog log = buildQueryLog(INST, NIN, DataType.CRIMINAL_RECORD, ExchangeResult.SUCCESS);
        log.setExchangedAt(LocalDateTime.now().withHour(23));

        service.analyze(log);

        verifyAlertCreated(AnomalyType.OFF_HOURS_SENSITIVE_ACCESS, AlertSeverity.HIGH);
    }

    @Test
    @DisplayName("Rule 3: non-sensitive data at 23:00 does NOT trigger off-hours alert")
    void analyze_offHoursNonSensitive_noAlert() {
        stubLowCounters(INST, NIN);
        ExchangeLog log = buildQueryLog(INST, NIN, DataType.TAX_STATUS, ExchangeResult.SUCCESS);
        log.setExchangedAt(LocalDateTime.now().withHour(23));

        service.analyze(log);

        verify(anomalyAlertRepository, never()).save(argThat(
                a -> a.getAlertType() == AnomalyType.OFF_HOURS_SENSITIVE_ACCESS));
    }

    // ── Rule 4: ROUTINE_CHECK on sensitive data without case reference ─────────

    @Test
    @DisplayName("Rule 4: CRIMINAL_RECORD + ROUTINE_CHECK + no caseReference → SELF_QUERY_SUSPICION MEDIUM")
    void analyze_selfQuerySuspicion_createsAlert() {
        stubLowCounters(INST, NIN);
        ExchangeLog log = buildQueryLog(INST, NIN, DataType.CRIMINAL_RECORD, ExchangeResult.SUCCESS);
        log.setPurposeCode(QueryPurpose.ROUTINE_CHECK);
        log.setCaseReference(null);
        // Force an off-hours-safe time to avoid that rule also firing
        log.setExchangedAt(LocalDateTime.now().withHour(10));

        service.analyze(log);

        verifyAlertCreated(AnomalyType.SELF_QUERY_SUSPICION, AlertSeverity.MEDIUM);
    }

    @Test
    @DisplayName("Rule 4: CRIMINAL_RECORD + ROUTINE_CHECK WITH caseReference does NOT trigger SELF_QUERY_SUSPICION")
    void analyze_routineCheckWithCaseRef_noAlert() {
        stubLowCounters(INST, NIN);
        ExchangeLog log = buildQueryLog(INST, NIN, DataType.CRIMINAL_RECORD, ExchangeResult.SUCCESS);
        log.setPurposeCode(QueryPurpose.ROUTINE_CHECK);
        log.setCaseReference("CASE-2024-001");
        log.setExchangedAt(LocalDateTime.now().withHour(10));

        service.analyze(log);

        verify(anomalyAlertRepository, never()).save(argThat(
                a -> a.getAlertType() == AnomalyType.SELF_QUERY_SUSPICION));
    }

    // ── Rule 5: Bulk citizen scan ─────────────────────────────────────────────

    @Test
    @DisplayName("Rule 5: >50 distinct citizens in 1 hour creates BULK_CITIZEN_SCAN CRITICAL alert")
    void analyze_bulkScan_createsCriticalAlert() {
        stubLowCounters(INST, NIN);
        when(exchangeLogRepository.countDistinctNationalIdsByInstitutionSince(
                eq(INST), any())).thenReturn(51L);

        service.analyze(buildQueryLog(INST, NIN, DataType.TAX_STATUS, ExchangeResult.SUCCESS));

        verifyAlertCreated(AnomalyType.BULK_CITIZEN_SCAN, AlertSeverity.CRITICAL);
    }

    // ── Rule 6: Repeated denied attempts ─────────────────────────────────────

    @Test
    @DisplayName("Rule 6: >5 DENIED results in 1 hour creates DENIED_REPEATED_ATTEMPT MEDIUM alert")
    void analyze_repeatedDenied_createsAlert() {
        stubLowCounters(INST, NIN);
        when(exchangeLogRepository.countDeniedByInstitutionSince(eq(INST), any())).thenReturn(6L);

        service.analyze(buildQueryLog(INST, NIN, DataType.TAX_STATUS, ExchangeResult.DENIED));

        verifyAlertCreated(AnomalyType.DENIED_REPEATED_ATTEMPT, AlertSeverity.MEDIUM);
    }

    // ── Deduplication ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("duplicate alert within 24 hours is silently skipped (no second save)")
    void analyze_duplicateWithin24h_notSavedAgain() {
        when(anomalyAlertRepository.existsByInstitutionCodeAndAlertTypeAndStatusAndDetectedAtAfter(
                eq(INST), eq(AnomalyType.HIGH_VOLUME_QUERIES), eq(AlertStatus.OPEN), any()))
                .thenReturn(true);
        when(exchangeLogRepository.countQueryByInstitutionSince(eq(INST), any())).thenReturn(30L);
        stubLowCounters(INST, NIN);

        service.analyze(buildQueryLog(INST, NIN, DataType.TAX_STATUS, ExchangeResult.SUCCESS));

        verify(anomalyAlertRepository, never()).save(argThat(
                a -> a.getAlertType() == AnomalyType.HIGH_VOLUME_QUERIES));
    }

    // ── PUBLISH operation ─────────────────────────────────────────────────────

    @Test
    @DisplayName("PUBLISH operations are ignored — no rules fire")
    void analyze_publishOperation_noRules() {
        ExchangeLog log = ExchangeLog.builder()
                .requestingCode(INST)
                .dataType(DataType.TAX_STATUS)
                .operationType(OperationType.PUBLISH)
                .result(ExchangeResult.SUCCESS)
                .exchangedAt(LocalDateTime.now())
                .build();

        service.analyze(log);

        verifyNoInteractions(anomalyAlertRepository);
        verifyNoInteractions(exchangeLogRepository);
    }

    // ── Notification sent to citizen ──────────────────────────────────────────

    @Test
    @DisplayName("notification is sent to citizen when an alert is created with a national ID")
    void analyze_alertWithNin_sendsNotification() {
        stubLowCounters(INST, NIN);
        when(exchangeLogRepository.countDistinctNationalIdsByInstitutionSince(
                eq(INST), any())).thenReturn(51L);

        ExchangeLog log = buildQueryLog(INST, NIN, DataType.TAX_STATUS, ExchangeResult.SUCCESS);
        log.setNationalId(NIN);
        service.analyze(log);

        verify(notificationClient).send(eq(NIN), eq("ANOMALY_DETECTED_ON_YOUR_DATA"),
                anyString(), anyMap());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ExchangeLog buildQueryLog(String code, String nationalId,
                                      DataType dt, ExchangeResult result) {
        return ExchangeLog.builder()
                .requestingCode(code)
                .nationalId(nationalId)
                .dataType(dt)
                .operationType(OperationType.QUERY)
                .result(result)
                .purposeCode(QueryPurpose.CRIMINAL_INVESTIGATION)
                .caseReference("CASE-001")
                .exchangedAt(LocalDateTime.now().withHour(10))
                .build();
    }

    private void stubLowCounters(String code, String nationalId) {
        when(exchangeLogRepository.countQueryByInstitutionSince(eq(code), any()))
                .thenReturn(0L);
        when(exchangeLogRepository.countQueryByInstitutionAndNationalIdSince(
                eq(code), anyString(), any())).thenReturn(0L);
        when(exchangeLogRepository.countDistinctNationalIdsByInstitutionSince(
                eq(code), any())).thenReturn(0L);
        when(exchangeLogRepository.countDeniedByInstitutionSince(eq(code), any()))
                .thenReturn(0L);
    }

    private void verifyAlertCreated(AnomalyType type, AlertSeverity severity) {
        ArgumentCaptor<AnomalyAlert> captor = ArgumentCaptor.forClass(AnomalyAlert.class);
        verify(anomalyAlertRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues())
                .anySatisfy(alert -> {
                    assertThat(alert.getAlertType()).isEqualTo(type);
                    assertThat(alert.getSeverity()).isEqualTo(severity);
                    assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
                });
    }
}
