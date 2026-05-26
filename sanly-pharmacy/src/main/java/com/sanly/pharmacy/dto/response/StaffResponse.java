package com.sanly.pharmacy.dto.response;

import com.sanly.pharmacy.entity.PharmacyStaff;
import com.sanly.pharmacy.entity.StaffRole;
import com.sanly.pharmacy.entity.StaffStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StaffResponse {
    private Long staffId;
    private String nationalId;
    private String firstName;
    private String lastName;
    private String username;
    private String pharmacyCode;
    private StaffRole role;
    private StaffStatus status;
    private LocalDateTime createdAt;

    public static StaffResponse from(PharmacyStaff s) {
        return StaffResponse.builder()
                .staffId(s.getStaffId())
                .nationalId(s.getNationalId())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .username(s.getUsername())
                .pharmacyCode(s.getPharmacyCode())
                .role(s.getRole())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
