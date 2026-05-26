package com.sanly.medical.dto.response;

import com.sanly.medical.entity.PharmacyStatus;
import com.sanly.medical.entity.RegisteredPharmacy;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PharmacyResponse {
    private Long pharmacyId;
    private String pharmacyCode;
    private String name;
    private String licenseNumber;
    private String region;
    private String address;
    private PharmacyStatus status;
    private LocalDateTime registeredAt;
    /** Only populated on first registration / key rotation */
    private String rawApiKey;

    public static PharmacyResponse from(RegisteredPharmacy p) {
        return PharmacyResponse.builder()
                .pharmacyId(p.getPharmacyId())
                .pharmacyCode(p.getPharmacyCode())
                .name(p.getName())
                .licenseNumber(p.getLicenseNumber())
                .region(p.getRegion())
                .address(p.getAddress())
                .status(p.getStatus())
                .registeredAt(p.getRegisteredAt())
                .build();
    }
}
