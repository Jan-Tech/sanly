package com.sanly.medical.dto.response;

import com.sanly.medical.entity.DoctorStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class DoctorResponse {
    private Long doctorId;
    private String nationalId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String licenseNumber;
    private Long clinicId;
    private String username;
    private Set<String> roles;
    private DoctorStatus status;
    private LocalDateTime createdAt;
}
