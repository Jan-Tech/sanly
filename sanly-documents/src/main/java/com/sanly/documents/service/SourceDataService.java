package com.sanly.documents.service;

import com.sanly.documents.entity.DocumentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SourceDataService {

    private final RestTemplate restTemplate;

    @Value("${services.civil-url:http://localhost:8087}")
    private String civilUrl;
    @Value("${services.civil-token}")
    private String civilToken;

    @Value("${services.dmv-url:http://localhost:8083}")
    private String dmvUrl;
    @Value("${services.dmv-token}")
    private String dmvToken;

    @Value("${services.education-url:http://localhost:8090}")
    private String educationUrl;
    @Value("${services.education-token}")
    private String educationToken;

    @Value("${services.land-url:http://localhost:8091}")
    private String landUrl;
    @Value("${services.land-token}")
    private String landToken;

    @Value("${services.business-url:http://localhost:8086}")
    private String businessUrl;
    @Value("${services.business-token}")
    private String businessToken;

    @Value("${services.tax-url:http://localhost:8085}")
    private String taxUrl;
    @Value("${services.tax-token}")
    private String taxToken;

    @Value("${services.police-url:http://localhost:8084}")
    private String policeUrl;
    @Value("${services.police-token}")
    private String policeToken;

    @Value("${services.vehicle-url:http://localhost:8096}")
    private String vehicleUrl;

    public SourceDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches document field data from the appropriate source service.
     * Returns a fallback map on any error.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> fetchDocumentData(DocumentType docType,
                                                  String sourceRecordCode,
                                                  String citizenToken) {
        try {
            return switch (docType) {
                case DIPLOMA -> get(educationUrl + "/api/v1/education/diplomas/" + sourceRecordCode,
                        educationToken);
                case DRIVING_LICENSE -> get(dmvUrl + "/api/v1/dmv/licenses/code/" + sourceRecordCode,
                        dmvToken);
                case BIRTH_CERTIFICATE -> get(civilUrl + "/api/v1/civil/birth/" + sourceRecordCode,
                        civilToken);
                case MARRIAGE_CERTIFICATE -> get(civilUrl + "/api/v1/civil/marriage/" + sourceRecordCode,
                        civilToken);
                case PROPERTY_DEED -> get(landUrl + "/api/v1/land/properties/" + sourceRecordCode,
                        landToken);
                case BUSINESS_REGISTRATION -> get(businessUrl + "/api/v1/business/companies/" + sourceRecordCode,
                        businessToken);
                case TAX_CLEARANCE -> get(taxUrl + "/api/v1/tax/taxpayers/" + sourceRecordCode,
                        taxToken);
                case CRIMINAL_CLEARANCE -> get(policeUrl + "/api/v1/police/records/citizen/" + sourceRecordCode,
                        policeToken);
                case VEHICLE_REGISTRATION -> getNoAuth(vehicleUrl + "/api/v1/vehicle/vehicles/" + sourceRecordCode + "/verify");
                default -> new HashMap<>();
            };
        } catch (Exception e) {
            log.warn("Source data unavailable for docType={} code={}: {}", docType, sourceRecordCode, e.getMessage());
            Map<String, String> fallback = new HashMap<>();
            fallback.put("note", "Source data unavailable — certificate generated from registry records");
            return fallback;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, String> get(String url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return extractData(response.getBody());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, String> getNoAuth(String url) {
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return extractData(response.getBody());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, String> extractData(Map body) {
        Map<String, String> result = new HashMap<>();
        if (body == null) return result;

        Object data = body.get("data");
        Map<String, Object> dataMap;

        if (data instanceof Map) {
            dataMap = (Map<String, Object>) data;
        } else {
            dataMap = (Map<String, Object>) body;
        }

        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            if (entry.getValue() instanceof String) {
                result.put(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return result;
    }
}
