package com.sanly.appointments.dto.response;

import com.sanly.appointments.entity.InstitutionType;
import com.sanly.appointments.entity.OfficeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OfficeResponse {

    private UUID officeId;
    private String officeCode;
    private InstitutionType institutionType;
    private String name;
    private String region;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private OfficeStatus status;
    private LocalDateTime createdAt;
}
