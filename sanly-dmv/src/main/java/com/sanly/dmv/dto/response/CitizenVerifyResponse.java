package com.sanly.dmv.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CitizenVerifyResponse {
    private String nationalId;
    private boolean exists;
    private boolean active;
    private String status;
    private String fullName;
}
