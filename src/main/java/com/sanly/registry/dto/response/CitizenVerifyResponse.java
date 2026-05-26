package com.sanly.registry.dto.response;

import com.sanly.registry.entity.CitizenStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CitizenVerifyResponse {
    private String nationalId;
    private boolean exists;
    private boolean active;
    private CitizenStatus status;
    private String fullName;
}
