package com.sanly.pharmacy.dto.response;

import com.sanly.pharmacy.entity.DispenseRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DispenseRecordResponse {
    private UUID recordId;
    private String prescriptionCode;
    private String citizenNationalId;
    private String pharmacyCode;
    private Long staffId;
    private LocalDateTime dispensedAt;
    private Integer quantityDispensed;
    private String notes;
    private boolean bridgeVerified;

    public static DispenseRecordResponse from(DispenseRecord d) {
        return DispenseRecordResponse.builder()
                .recordId(d.getRecordId())
                .prescriptionCode(d.getPrescriptionCode())
                .citizenNationalId(d.getCitizenNationalId())
                .pharmacyCode(d.getPharmacyCode())
                .staffId(d.getStaffId())
                .dispensedAt(d.getDispensedAt())
                .quantityDispensed(d.getQuantityDispensed())
                .notes(d.getNotes())
                .bridgeVerified(d.isBridgeVerified())
                .build();
    }
}
