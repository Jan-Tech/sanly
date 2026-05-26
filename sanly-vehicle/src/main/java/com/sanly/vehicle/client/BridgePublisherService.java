package com.sanly.vehicle.client;

import com.sanly.vehicle.entity.InsuranceRecord;
import com.sanly.vehicle.entity.InsuranceStatus;
import com.sanly.vehicle.entity.TechnicalInspection;
import com.sanly.vehicle.entity.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class BridgePublisherService {
    private final RestTemplate restTemplate;
    private final String bridgeBaseUrl;
    private final String institutionCode;
    private final String institutionKey;

    public BridgePublisherService(RestTemplate restTemplate,
                                   @Value("${sanly.bridge.base-url}") String bridgeBaseUrl,
                                   @Value("${sanly.bridge.institution-code}") String institutionCode,
                                   @Value("${sanly.bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate;
        this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionCode = institutionCode;
        this.institutionKey = institutionKey;
    }

    @Async("bridgePublishExecutor")
    public void publishVehicleRecord(Vehicle vehicle, String ownerNationalId,
                                      InsuranceRecord activeInsurance, TechnicalInspection lastInspection) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("plateNumber", vehicle.getPlateNumber());
            payload.put("vin", vehicle.getVin());
            payload.put("ownerNationalId", ownerNationalId);
            payload.put("vehicleType", vehicle.getVehicleType().name());
            payload.put("make", vehicle.getMake());
            payload.put("model", vehicle.getModel());
            payload.put("year", vehicle.getYear());
            payload.put("status", vehicle.getStatus().name());
            payload.put("insuranceStatus", activeInsurance != null ? activeInsurance.getStatus().name() : "NONE");
            payload.put("lastInspectionDate", lastInspection != null ? lastInspection.getInspectionDate().toString() : null);

            Map<String, Object> body = Map.of(
                    "subjectNationalId", ownerNationalId != null ? ownerNationalId : "UNKNOWN",
                    "dataType", "VEHICLE_RECORD",
                    "recordRef", vehicle.getPlateNumber(),
                    "payload", payload
            );

            restTemplate.postForEntity(bridgeBaseUrl + "/api/v1/exchange/publish",
                    new HttpEntity<>(body, headers), Void.class);
            log.info("[BRIDGE] Published VEHICLE_RECORD for plate={}", vehicle.getPlateNumber());
        } catch (Exception ex) {
            log.warn("[BRIDGE] Failed to publish VEHICLE_RECORD for plate={}: {}",
                    vehicle.getPlateNumber(), ex.getMessage());
        }
    }
}
