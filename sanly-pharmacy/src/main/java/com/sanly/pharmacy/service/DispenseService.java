package com.sanly.pharmacy.service;

import com.sanly.pharmacy.client.BridgeQueryClient;
import com.sanly.pharmacy.client.CitizenRegistryClient;
import com.sanly.pharmacy.client.MedicalClient;
import com.sanly.pharmacy.client.NotificationClient;
import com.sanly.pharmacy.config.UserDetailsImpl;
import com.sanly.pharmacy.dto.request.DispenseRequest;
import com.sanly.pharmacy.dto.response.DispenseRecordResponse;
import com.sanly.pharmacy.entity.DispenseRecord;
import com.sanly.pharmacy.repository.DispenseRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispenseService {

    private final DispenseRecordRepository dispensingRepo;
    private final BridgeQueryClient        bridgeQueryClient;
    private final CitizenRegistryClient    citizenRegistryClient;
    private final MedicalClient            medicalClient;
    private final NotificationClient       notificationClient;

    @Value("${sanly.medical.pharmacy-code:TM-PHR-0001}")
    private String pharmacyCode;

    @Value("${sanly.medical.base-url:}")
    private String pharmacyName;

    /**
     * Full dispense flow:
     * 1. Verify prescription ACTIVE via bridge
     * 2. Call sanly-medical to record dispensing
     * 3. Save local DispenseRecord
     * 4. Notify citizen
     */
    @Transactional
    public DispenseRecordResponse dispense(DispenseRequest req, UserDetailsImpl staff) {
        String prescriptionCode = req.getPrescriptionCode();

        // 1. Bridge verification — find the prescription summary
        String citizenNationalId = findCitizenNationalId(prescriptionCode);
        Map<?, ?> summary        = findPrescriptionSummary(prescriptionCode, citizenNationalId);

        if (summary == null) {
            throw new IllegalStateException("Prescription not found in bridge: " + prescriptionCode);
        }

        String status = (String) summary.get("status");
        if ("CANCELLED".equals(status)) {
            throw new IllegalStateException("Prescription has been cancelled");
        }
        if ("FULLY_DISPENSED".equals(status)) {
            throw new IllegalStateException("This prescription has been fully dispensed");
        }
        String expiresAtStr = (String) summary.get("expiresAt");
        if (expiresAtStr != null && LocalDate.now().isAfter(LocalDate.parse(expiresAtStr))) {
            throw new IllegalStateException("Prescription has expired");
        }

        // 2. Record on sanly-medical (PharmacyKeyAuthFilter verifies the call)
        boolean bridgeVerified = true;
        try {
            medicalClient.dispense(prescriptionCode, req.getQuantityDispensed(),
                    staff != null ? String.valueOf(staff.getStaffId()) : null,
                    req.getNotes());
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Medical client dispense failed: {}", ex.getMessage());
            bridgeVerified = false;
        }

        // 3. Save local record
        DispenseRecord record = DispenseRecord.builder()
                .prescriptionCode(prescriptionCode)
                .citizenNationalId(citizenNationalId != null ? citizenNationalId : "unknown")
                .pharmacyCode(pharmacyCode)
                .staffId(staff != null ? staff.getStaffId() : 0L)
                .dispensedAt(LocalDateTime.now())
                .quantityDispensed(req.getQuantityDispensed())
                .notes(req.getNotes())
                .bridgeVerified(bridgeVerified)
                .build();
        DispenseRecord saved = dispensingRepo.save(record);

        // 4. Notify citizen
        if (citizenNationalId != null) {
            String medicationName = (String) summary.get("medicationName");
            notificationClient.send(citizenNationalId, "PRESCRIPTION_DISPENSED", "EN",
                    Map.of(
                            "pharmacyName",      pharmacyCode,
                            "medicationName",    medicationName != null ? medicationName : "medication",
                            "quantityDispensed", String.valueOf(req.getQuantityDispensed()),
                            "dispensedAt",       saved.getDispensedAt().toString(),
                            "prescriptionCode",  prescriptionCode
                    ));
        }

        log.info("Dispensed prescription {} at pharmacy={} staff={}", prescriptionCode, pharmacyCode,
                staff != null ? staff.getStaffId() : "unknown");
        return DispenseRecordResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<DispenseRecordResponse> getHistory() {
        return dispensingRepo.findByPharmacyCodeOrderByDispensedAtDesc(pharmacyCode)
                .stream().map(DispenseRecordResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DispenseRecordResponse> getHistoryByCitizen(String nationalId) {
        return dispensingRepo.findByCitizenNationalIdAndPharmacyCodeOrderByDispensedAtDesc(nationalId, pharmacyCode)
                .stream().map(DispenseRecordResponse::from).toList();
    }

    /**
     * Searches bridge records to find the citizen NIN associated with a prescription code.
     * This requires the pharmacist to know either the prescription code or the citizen NIN.
     * In the direct dispense flow, the citizen provides their NIN at the counter.
     */
    private String findCitizenNationalId(String prescriptionCode) {
        // In a real call the pharmacist provides the NIN at the counter;
        // for the API flow, it comes from the request or the bridge lookup.
        // This method tries the recent local dispense records for continuity.
        return dispensingRepo.findByPharmacyCodeOrderByDispensedAtDesc(pharmacyCode)
                .stream()
                .filter(d -> prescriptionCode.equals(d.getPrescriptionCode()))
                .map(DispenseRecord::getCitizenNationalId)
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> findPrescriptionSummary(String code, String citizenNin) {
        if (citizenNin == null) return null;
        List<Map<String, Object>> records = bridgeQueryClient.queryPrescriptions(citizenNin);
        for (Map<String, Object> record : records) {
            Object summary = record.get("summary");
            if (summary instanceof Map<?, ?> s && code.equals(s.get("prescriptionCode"))) {
                return s;
            }
        }
        return null;
    }
}
