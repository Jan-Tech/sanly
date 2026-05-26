package com.sanly.bridge.service;

import com.sanly.bridge.dto.request.ExchangePublishRequest;
import com.sanly.bridge.dto.request.ExchangeQueryRequest;
import com.sanly.bridge.dto.response.ExchangeLogResponse;
import com.sanly.bridge.dto.response.ExchangeResponse;
import com.sanly.bridge.dto.response.PageResponse;
import com.sanly.bridge.dto.response.PublishedDataResponse;
import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.ExchangeResult;
import com.sanly.bridge.entity.QueryPurpose;

import java.time.LocalDateTime;
import java.util.List;

public interface ExchangeService {

    /**
     * Process a data query from an authenticated institution.
     * Enforces permission check before returning any data.
     * purposeCode is required and logged; caseReference is required for sensitive data types.
     */
    ExchangeResponse query(String requestingCode, ExchangeQueryRequest request);

    /**
     * Publish a data record from an authenticated institution into SANLY Bridge.
     * Enforces that the institution is authorized to publish this data type.
     */
    ExchangeResponse publish(String publisherCode, ExchangePublishRequest request);

    /**
     * Direct data lookup — same permission enforcement as {@link #query}.
     * Convenience GET endpoint. purposeCode is required; caseReference required for sensitive types.
     */
    List<PublishedDataResponse> getData(String requestingCode, String nationalId, DataType dataType,
                                        QueryPurpose purposeCode, String caseReference, String justification);

    PageResponse<ExchangeLogResponse> getAuditLog(
            String institutionCode, String nationalId, DataType dataType,
            ExchangeResult result, LocalDateTime from, LocalDateTime to,
            int page, int size);

    PageResponse<ExchangeLogResponse> getAuditLogByNationalId(
            String nationalId, int page, int size);
}
