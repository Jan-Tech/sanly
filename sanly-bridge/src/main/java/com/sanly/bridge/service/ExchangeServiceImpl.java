package com.sanly.bridge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanly.bridge.dto.request.ExchangePublishRequest;
import com.sanly.bridge.dto.request.ExchangeQueryRequest;
import com.sanly.bridge.dto.response.ExchangeLogResponse;
import com.sanly.bridge.dto.response.ExchangeResponse;
import com.sanly.bridge.dto.response.PageResponse;
import com.sanly.bridge.dto.response.PublishedDataResponse;
import com.sanly.bridge.entity.*;
import com.sanly.bridge.exception.InvalidExchangeRequestException;
import com.sanly.bridge.exception.InstitutionNotFoundException;
import com.sanly.bridge.exception.InstitutionSuspendedException;
import com.sanly.bridge.exception.PermissionDeniedException;
import com.sanly.bridge.client.NotificationClient;
import com.sanly.bridge.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {

    private final InstitutionRepository           institutionRepository;
    private final InstitutionPermissionRepository permissionRepository;
    private final PublishedDataRepository         publishedDataRepository;
    private final ExchangeLogRepository           exchangeLogRepository;
    private final AnomalyDetectionService         anomalyDetectionService;
    private final NotificationClient              notificationClient;
    private final ObjectMapper                    objectMapper = new ObjectMapper();

    /** Data types that require a non-blank caseReference on every query. */
    private static final Set<DataType> SENSITIVE_TYPES =
            Set.of(DataType.CRIMINAL_RECORD, DataType.MEDICAL_CLEARANCE);

    // ===== Query =====

    @Override
    @Transactional
    public ExchangeResponse query(String requestingCode, ExchangeQueryRequest req) {
        long start = System.currentTimeMillis();

        // purposeCode is validated @NotNull by @Valid; sensitive types need caseReference too.
        validateSensitiveAccess(req.getDataType(), req.getCaseReference());

        requireActiveInstitution(requestingCode);

        String targetCode = req.getTargetCode();
        boolean hasTargetFilter = StringUtils.hasText(targetCode);

        boolean permitted;
        if (hasTargetFilter) {
            permitted = permissionRepository
                    .existsByRequestingCodeAndTargetCodeAndDataTypeAndActiveTrue(
                            requestingCode, targetCode, req.getDataType());
        } else {
            permitted = !permissionRepository
                    .findAllByRequestingCodeAndActiveTrue(requestingCode)
                    .stream()
                    .filter(p -> p.getDataType() == req.getDataType())
                    .toList()
                    .isEmpty();
        }

        if (!permitted) {
            int ms = elapsed(start);
            ExchangeLog deniedEntry = persistLog(
                    requestingCode, targetCode, req.getNationalId(), req.getDataType(),
                    OperationType.QUERY, ExchangeResult.DENIED, ms,
                    "No active permission for " + req.getDataType(),
                    req.getPurposeCode(), req.getCaseReference(), req.getJustification());
            anomalyDetectionService.analyze(deniedEntry);
            throw new PermissionDeniedException(requestingCode, req.getDataType());
        }

        List<PublishedData> records = hasTargetFilter
                ? publishedDataRepository.findActiveByPublisher(
                        req.getNationalId(), req.getDataType(), targetCode, LocalDateTime.now())
                : publishedDataRepository.findActive(
                        req.getNationalId(), req.getDataType(), LocalDateTime.now());

        ExchangeResult result = records.isEmpty()
                ? ExchangeResult.NOT_FOUND : ExchangeResult.SUCCESS;

        int ms = elapsed(start);
        ExchangeLog entry = persistLog(
                requestingCode, targetCode, req.getNationalId(), req.getDataType(),
                OperationType.QUERY, result, ms, null,
                req.getPurposeCode(), req.getCaseReference(), req.getJustification());
        anomalyDetectionService.analyze(entry);

        // Notify the citizen that their data was accessed (only on SUCCESS, non-null NIN)
        if (result == ExchangeResult.SUCCESS && StringUtils.hasText(req.getNationalId())) {
            dispatchAccessNotification(requestingCode, req.getNationalId(),
                    req.getDataType(), entry);
        }

        return ExchangeResponse.builder()
                .exchangeId(entry.getId())
                .requestingCode(requestingCode)
                .targetCode(targetCode)
                .nationalId(req.getNationalId())
                .dataType(req.getDataType())
                .operationType(OperationType.QUERY)
                .result(result)
                .records(records.stream().map(this::toPublishedDataResponse).toList())
                .responseTimeMs(ms)
                .exchangedAt(entry.getExchangedAt())
                .build();
    }

    // ===== Publish =====

    @Override
    @Transactional
    public ExchangeResponse publish(String publisherCode, ExchangePublishRequest req) {
        long start = System.currentTimeMillis();

        Institution institution = requireActiveInstitution(publisherCode);

        if (!institution.getPublishableTypes().contains(req.getDataType())) {
            int ms = elapsed(start);
            persistLog(publisherCode, null, req.getNationalId(), req.getDataType(),
                    OperationType.PUBLISH, ExchangeResult.DENIED, ms,
                    publisherCode + " is not authorized to publish " + req.getDataType(),
                    null, null, null);
            throw new PermissionDeniedException(
                    publisherCode + " is not authorized to publish " + req.getDataType());
        }

        PublishedData data = PublishedData.builder()
                .publisherCode(publisherCode)
                .nationalId(req.getNationalId())
                .dataType(req.getDataType())
                .recordRef(req.getRecordRef())
                .summary(serializeSummary(req.getSummary()))
                .publishedAt(LocalDateTime.now())
                .expiresAt(req.getExpiresAt())
                .active(true)
                .build();

        PublishedData saved = publishedDataRepository.save(data);
        int ms = elapsed(start);

        ExchangeLog exchangeEntry = persistLog(publisherCode, null, req.getNationalId(),
                req.getDataType(), OperationType.PUBLISH, ExchangeResult.SUCCESS, ms, null,
                null, null, null);

        log.info("Published {} for NIN={} by {}", req.getDataType(), req.getNationalId(), publisherCode);

        return ExchangeResponse.builder()
                .exchangeId(exchangeEntry.getId())
                .requestingCode(publisherCode)
                .nationalId(req.getNationalId())
                .dataType(req.getDataType())
                .operationType(OperationType.PUBLISH)
                .result(ExchangeResult.SUCCESS)
                .records(List.of(toPublishedDataResponse(saved)))
                .responseTimeMs(ms)
                .exchangedAt(exchangeEntry.getExchangedAt())
                .build();
    }

    // ===== Direct data lookup =====

    @Override
    @Transactional
    public List<PublishedDataResponse> getData(String requestingCode, String nationalId,
                                               DataType dataType, QueryPurpose purposeCode,
                                               String caseReference, String justification) {
        long start = System.currentTimeMillis();

        validateSensitiveAccess(dataType, caseReference);
        requireActiveInstitution(requestingCode);

        boolean permitted = !permissionRepository
                .findAllByRequestingCodeAndActiveTrue(requestingCode)
                .stream()
                .filter(p -> p.getDataType() == dataType)
                .toList()
                .isEmpty();

        if (!permitted) {
            ExchangeLog deniedEntry = persistLog(requestingCode, null, nationalId, dataType,
                    OperationType.QUERY, ExchangeResult.DENIED, elapsed(start),
                    "No active permission for " + dataType,
                    purposeCode, caseReference, justification);
            anomalyDetectionService.analyze(deniedEntry);
            throw new PermissionDeniedException(requestingCode, dataType);
        }

        List<PublishedData> records = publishedDataRepository
                .findActive(nationalId, dataType, LocalDateTime.now());

        ExchangeResult result = records.isEmpty()
                ? ExchangeResult.NOT_FOUND : ExchangeResult.SUCCESS;
        ExchangeLog entry = persistLog(requestingCode, null, nationalId, dataType,
                OperationType.QUERY, result, elapsed(start), null,
                purposeCode, caseReference, justification);
        anomalyDetectionService.analyze(entry);

        return records.stream().map(this::toPublishedDataResponse).toList();
    }

    // ===== Audit =====

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExchangeLogResponse> getAuditLog(
            String institutionCode, String nationalId, DataType dataType,
            ExchangeResult result, LocalDateTime from, LocalDateTime to,
            int page, int size) {

        Page<ExchangeLogResponse> pg = exchangeLogRepository
                .findWithFilters(
                        StringUtils.hasText(institutionCode) ? institutionCode : null,
                        StringUtils.hasText(nationalId)      ? nationalId      : null,
                        dataType, result, from, to,
                        PageRequest.of(page, size))
                .map(this::toLogResponse);

        return toPageResponse(pg);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExchangeLogResponse> getAuditLogByNationalId(
            String nationalId, int page, int size) {

        Page<ExchangeLogResponse> pg = exchangeLogRepository
                .findByNationalIdOrderByExchangedAtDesc(nationalId, PageRequest.of(page, size))
                .map(this::toLogResponse);

        return toPageResponse(pg);
    }

    // ===== Private helpers =====

    /** Sensitive data types (CRIMINAL_RECORD, MEDICAL_CLEARANCE) require a non-blank caseReference. */
    private void validateSensitiveAccess(DataType dataType, String caseReference) {
        if (SENSITIVE_TYPES.contains(dataType) && !StringUtils.hasText(caseReference)) {
            throw new InvalidExchangeRequestException(
                    "caseReference is required for sensitive data type " + dataType);
        }
    }

    private Institution requireActiveInstitution(String code) {
        Institution inst = institutionRepository.findByInstitutionCode(code)
                .orElseThrow(() -> new InstitutionNotFoundException(code));
        if (inst.getStatus() == InstitutionStatus.SUSPENDED) {
            throw new InstitutionSuspendedException(code);
        }
        return inst;
    }

    private ExchangeLog persistLog(String requestingCode, String targetCode,
                                    String nationalId, DataType dataType,
                                    OperationType opType, ExchangeResult result,
                                    int responseTimeMs, String details,
                                    QueryPurpose purposeCode, String caseReference,
                                    String justification) {
        ExchangeLog entry = ExchangeLog.builder()
                .requestingCode(requestingCode)
                .targetCode(targetCode)
                .nationalId(nationalId)
                .dataType(dataType)
                .operationType(opType)
                .result(result)
                .responseTimeMs(responseTimeMs)
                .details(details)
                .purposeCode(purposeCode)
                .caseReference(caseReference)
                .justification(justification)
                .exchangedAt(LocalDateTime.now())
                .build();
        return exchangeLogRepository.save(entry);
    }

    private PublishedDataResponse toPublishedDataResponse(PublishedData p) {
        return PublishedDataResponse.builder()
                .id(p.getId())
                .publisherCode(p.getPublisherCode())
                .nationalId(p.getNationalId())
                .dataType(p.getDataType())
                .recordRef(p.getRecordRef())
                .summary(deserializeSummary(p.getSummary()))
                .publishedAt(p.getPublishedAt())
                .expiresAt(p.getExpiresAt())
                .active(p.isActive())
                .build();
    }

    private ExchangeLogResponse toLogResponse(ExchangeLog l) {
        return ExchangeLogResponse.builder()
                .id(l.getId())
                .requestingCode(l.getRequestingCode())
                .targetCode(l.getTargetCode())
                .nationalId(l.getNationalId())
                .dataType(l.getDataType())
                .operationType(l.getOperationType())
                .result(l.getResult())
                .responseTimeMs(l.getResponseTimeMs())
                .details(l.getDetails())
                .exchangedAt(l.getExchangedAt())
                .purposeCode(l.getPurposeCode())
                .caseReference(l.getCaseReference())
                .build();
    }

    private String serializeSummary(JsonNode node) {
        if (node == null) return null;
        try { return objectMapper.writeValueAsString(node); }
        catch (JsonProcessingException e) { return node.toString(); }
    }

    private JsonNode deserializeSummary(String json) {
        if (json == null) return null;
        try { return objectMapper.readTree(json); }
        catch (JsonProcessingException e) { return null; }
    }

    private static int elapsed(long startMs) {
        return (int) (System.currentTimeMillis() - startMs);
    }

    private <T> PageResponse<T> toPageResponse(Page<T> p) {
        return PageResponse.<T>builder()
                .content(p.getContent())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }

    /** Maps a requesting institution code to its DATA_ACCESSED_BY_* notification event type. */
    private void dispatchAccessNotification(String requestingCode, String nationalId,
                                             DataType dataType, ExchangeLog entry) {
        String eventType = switch (requestingCode) {
            case "INST_POLICE"   -> "DATA_ACCESSED_BY_POLICE";
            case "INST_TAX"      -> "DATA_ACCESSED_BY_TAX";
            case "INST_DMV"      -> "DATA_ACCESSED_BY_DMV";
            case "INST_MEDICAL"  -> "DATA_ACCESSED_BY_MEDICAL";
            case "INST_BUSINESS" -> "DATA_ACCESSED_BY_BUSINESS";
            default              -> "DATA_ACCESSED_UNKNOWN_INSTITUTION";
        };

        String purposeCode    = entry.getPurposeCode()    != null ? entry.getPurposeCode().name()    : "UNKNOWN";
        String caseReference  = StringUtils.hasText(entry.getCaseReference()) ? entry.getCaseReference() : "N/A";
        String accessedAt     = entry.getExchangedAt()    != null ? entry.getExchangedAt().toString() : "";

        notificationClient.send(nationalId, eventType, "EN",
                Map.of(
                        "accessedAt",    accessedAt,
                        "dataType",      dataType.name(),
                        "purposeCode",   purposeCode,
                        "caseReference", caseReference
                ));
    }
}
