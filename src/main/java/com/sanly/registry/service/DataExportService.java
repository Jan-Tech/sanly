package com.sanly.registry.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanly.registry.entity.Citizen;
import com.sanly.registry.repository.CitizenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataExportService {

    private static final List<String> SERVICES_WITH_DATA = List.of(
            "MEDICAL", "DMV", "POLICE", "TAX", "BUSINESS", "CIVIL",
            "EDUCATION", "LAND", "SOCIAL", "CUSTOMS", "COURT",
            "VEHICLE", "PENSION", "SIGNATURE"
    );

    private final CitizenRepository citizenRepository;
    private final ObjectMapper objectMapper;

    public String compileExport(String nationalId) {
        try {
            Citizen citizen = citizenRepository.findById(nationalId).orElse(null);

            Map<String, Object> citizenData = new LinkedHashMap<>();
            if (citizen != null) {
                citizenData.put("nationalId", citizen.getNationalId());
                citizenData.put("firstName", citizen.getFirstName());
                citizenData.put("lastName", citizen.getLastName());
                citizenData.put("status", citizen.getStatus() != null ? citizen.getStatus().name() : null);
                citizenData.put("createdAt", citizen.getCreatedAt() != null ? citizen.getCreatedAt().toString() : null);
            } else {
                citizenData.put("nationalId", nationalId);
                citizenData.put("note", "Citizen record not found at time of export");
            }

            Map<String, Object> export = new LinkedHashMap<>();
            export.put("citizen", citizenData);
            export.put("servicesWithData", SERVICES_WITH_DATA);
            export.put("note", "This export contains metadata only. Detailed records from each service must be requested separately.");
            export.put("exportedAt", LocalDateTime.now().toString());

            return objectMapper.writeValueAsString(export);

        } catch (Exception ex) {
            log.error("[EXPORT] Failed to compile data export for nationalId={}: {}", nationalId, ex.getMessage(), ex);
            try {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Export partially failed");
                error.put("citizenId", nationalId);
                error.put("exportedAt", LocalDateTime.now().toString());
                return objectMapper.writeValueAsString(error);
            } catch (Exception jsonEx) {
                return "{\"error\": \"Export partially failed\", \"citizenId\": \"" + nationalId + "\", \"exportedAt\": \"" + LocalDateTime.now() + "\"}";
            }
        }
    }
}
