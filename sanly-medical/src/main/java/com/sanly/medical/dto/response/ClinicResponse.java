package com.sanly.medical.dto.response;

import com.sanly.medical.entity.ClinicStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClinicResponse {
    private Long clinicId;
    private String name;
    private String licenseNumber;
    private String region;
    private String address;
    private String phone;
    private ClinicStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
