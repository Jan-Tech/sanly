package com.sanly.pharmacy.service;

import com.sanly.pharmacy.client.BridgeQueryClient;
import com.sanly.pharmacy.client.CitizenRegistryClient;
import com.sanly.pharmacy.dto.response.PrescriptionLookupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionLookupService {

    private final BridgeQueryClient      bridgeQueryClient;
    private final CitizenRegistryClient  citizenRegistryClient;

    /**
     * Looks up a specific prescription by code via SANLY Bridge.
     * Returns null if not found.
     */
    public PrescriptionLookupResponse getByCode(String prescriptionCode, String citizenNationalId) {
        List<Map<String, Object>> records = bridgeQueryClient.queryPrescriptions(citizenNationalId);
        for (Map<String, Object> record : records) {
            Object summary = record.get("summary");
            if (summary instanceof Map<?, ?> s) {
                if (prescriptionCode.equals(s.get("prescriptionCode"))) {
                    return buildResponse(citizenNationalId, s);
                }
            }
        }
        return null;
    }

    /**
     * Gets all ACTIVE prescriptions for a citizen (by NIN lookup).
     */
    public List<PrescriptionLookupResponse> getActiveByCitizen(String nationalId) {
        List<Map<String, Object>> records = bridgeQueryClient.queryPrescriptions(nationalId);
        List<PrescriptionLookupResponse> result = new ArrayList<>();
        for (Map<String, Object> record : records) {
            Object summary = record.get("summary");
            if (summary instanceof Map<?, ?> s) {
                String status = (String) s.get("status");
                if ("ACTIVE".equals(status) || "PARTIALLY_DISPENSED".equals(status)) {
                    result.add(buildResponse(nationalId, s));
                }
            }
        }
        return result;
    }

    private PrescriptionLookupResponse buildResponse(String nationalId, Map<?, ?> s) {
        String citizenName = citizenRegistryClient.getCitizenFullName(nationalId);
        String expiresAtStr = (String) s.get("expiresAt");
        LocalDate expiresAt = expiresAtStr != null ? LocalDate.parse(expiresAtStr) : null;
        boolean expired = expiresAt != null && LocalDate.now().isAfter(expiresAt);

        int refillsAllowed = toInt(s.get("refillsAllowed"));
        int refillsUsed    = toInt(s.get("refillsUsed"));

        return PrescriptionLookupResponse.builder()
                .prescriptionCode((String) s.get("prescriptionCode"))
                .citizenNationalId(nationalId)
                .citizenFullName(citizenName != null ? citizenName : "Unknown")
                .medicationName((String) s.get("medicationName"))
                .medicationDosage((String) s.get("medicationDosage"))
                .status((String) s.get("status"))
                .refillsAllowed(refillsAllowed)
                .refillsUsed(refillsUsed)
                .refillsRemaining(Math.max(0, refillsAllowed - refillsUsed))
                .expiresAt(expiresAt)
                .expired(expired)
                .build();
    }

    private int toInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        if (val instanceof String s)  return Integer.parseInt(s);
        return 0;
    }
}
