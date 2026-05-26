package com.sanly.bridge.dto.response;

import com.sanly.bridge.entity.DataType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PermissionResponse {
    private Long id;
    private String requestingCode;
    private String targetCode;
    private DataType dataType;
    private String grantedBy;
    private LocalDateTime grantedAt;
    private boolean active;
}
