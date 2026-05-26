package com.sanly.medical.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Proxied response from SANLY Citizen Registry verify endpoint.
 */
@Data
@Builder
public class CitizenVerifyResponse {
    private String nationalId;
    private boolean exists;
    private boolean active;
    private String status;
    private String fullName;
}
