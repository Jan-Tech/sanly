package com.sanly.bridge.dto.response;

import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.ExchangeResult;
import com.sanly.bridge.entity.OperationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExchangeResponse {
    private Long exchangeId;
    private String requestingCode;
    private String targetCode;
    private String nationalId;
    private DataType dataType;
    private OperationType operationType;
    private ExchangeResult result;
    private List<PublishedDataResponse> records;
    private int responseTimeMs;
    private LocalDateTime exchangedAt;
}
