package com.sanly.civil.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class EducationNotifier {

    private final RestTemplate restTemplate;
    private final String educationBaseUrl;
    private final String lifeEventKey;

    public EducationNotifier(RestTemplate restTemplate,
                              @Value("${life-event.education-url:http://localhost:8090}") String educationBaseUrl,
                              @Value("${life-event.service-key:change-me-life-event-key}") String lifeEventKey) {
        this.restTemplate = restTemplate;
        this.educationBaseUrl = educationBaseUrl;
        this.lifeEventKey = lifeEventKey;
    }

    @Async("lifeEventExecutor")
    public void queueForEnrollment(String childNationalId, int expectedSchoolYear) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Life-Event-Key", lifeEventKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "childNationalId",    childNationalId,
                    "expectedSchoolYear", expectedSchoolYear
            );

            restTemplate.postForEntity(
                    educationBaseUrl + "/api/v1/education/enrollments/pending-intake",
                    new HttpEntity<>(body, headers),
                    Void.class);

            log.info("[EDUCATION] Child NIN={} queued for school enrollment in year {}",
                    childNationalId, expectedSchoolYear);
        } catch (Exception ex) {
            log.warn("[EDUCATION] Failed to queue child NIN={} for enrollment: {}",
                    childNationalId, ex.getMessage());
        }
    }
}
