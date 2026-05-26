package com.sanly.medical.dto.response;

import com.sanly.medical.entity.RecordResult;
import com.sanly.medical.entity.TestType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MedicalRecordResponse {
    private UUID recordId;
    private String citizenNationalId;
    private Long doctorId;
    private Long clinicId;
    private TestType testType;
    private RecordResult result;
    private String notes;
    private LocalDateTime testedAt;
    private LocalDateTime expiresAt;
    private boolean bridgePublished;
    private LocalDateTime bridgePublishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
