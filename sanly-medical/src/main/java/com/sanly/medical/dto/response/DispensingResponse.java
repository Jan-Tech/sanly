package com.sanly.medical.dto.response;

import com.sanly.medical.entity.PrescriptionDispensing;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DispensingResponse {
    private UUID dispensingId;
    private String prescriptionCode;
    private String dispensedByPharmacyCode;
    private LocalDateTime dispensedAt;
    private Integer quantityDispensed;
    private String pharmacistNationalId;
    private String notes;

    public static DispensingResponse from(PrescriptionDispensing d) {
        return DispensingResponse.builder()
                .dispensingId(d.getDispensingId())
                .prescriptionCode(d.getPrescriptionCode())
                .dispensedByPharmacyCode(d.getDispensedByPharmacyCode())
                .dispensedAt(d.getDispensedAt())
                .quantityDispensed(d.getQuantityDispensed())
                .pharmacistNationalId(d.getPharmacistNationalId())
                .notes(d.getNotes())
                .build();
    }
}
