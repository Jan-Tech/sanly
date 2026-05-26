package com.sanly.registry.integration;

import com.sanly.registry.AbstractIntegrationTest;
import com.sanly.registry.client.NotificationClient;
import com.sanly.registry.dto.request.CitizenCreateRequest;
import com.sanly.registry.dto.request.StatusUpdateRequest;
import com.sanly.registry.dto.response.ApiResponse;
import com.sanly.registry.dto.response.CitizenResponse;
import com.sanly.registry.dto.response.CitizenVerifyResponse;
import com.sanly.registry.dto.response.LoginResponse;
import com.sanly.registry.dto.response.PageResponse;
import com.sanly.registry.entity.CitizenStatus;
import com.sanly.registry.entity.Gender;
import com.sanly.registry.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for the citizen-registry service.
 *
 * A real PostgreSQL container is started via AbstractIntegrationTest.
 * Flyway migrations run on first context load. External HTTP clients
 * (NotificationClient, OtpService) are mocked to prevent network calls.
 */
@DisplayName("CitizenRegistry integration")
class CitizenRegistryIntegrationTest extends AbstractIntegrationTest {

    @Autowired TestRestTemplate restTemplate;

    @MockBean NotificationClient notificationClient;
    @MockBean OtpService         otpService;

    private String adminToken;

    @BeforeEach
    void loginAsAdmin() {
        Map<String, String> creds = Map.of("username", "admin", "password", "TestAdmin123!");
        ResponseEntity<ApiResponse<LoginResponse>> resp =
                restTemplate.exchange("/api/v1/auth/login",
                        HttpMethod.POST,
                        new HttpEntity<>(creds, jsonHeaders()),
                        new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        adminToken = resp.getBody().getData().getToken();
        assertThat(adminToken).isNotBlank();
    }

    // ── Register → retrieve ───────────────────────────────────────────────────

    @Test
    @DisplayName("register creates citizen; getByNationalId returns same record")
    void registerThenGet_roundTrip() {
        CitizenCreateRequest req = buildCreateRequest("Merdan", "Atayew");

        ResponseEntity<ApiResponse<CitizenResponse>> createResp =
                restTemplate.exchange("/api/v1/citizens",
                        HttpMethod.POST,
                        new HttpEntity<>(req, authHeaders()),
                        new ParameterizedTypeReference<>() {});

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String nin = createResp.getBody().getData().getNationalId();
        assertThat(nin).matches("\\d{11}");

        ResponseEntity<ApiResponse<CitizenResponse>> getResp =
                restTemplate.exchange("/api/v1/citizens/{nin}",
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders()),
                        new ParameterizedTypeReference<ApiResponse<CitizenResponse>>() {},
                        nin);

        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        CitizenResponse body = getResp.getBody().getData();
        assertThat(body.getFirstName()).isEqualTo("Merdan");
        assertThat(body.getStatus()).isEqualTo(CitizenStatus.ACTIVE);
    }

    // ── Verify active / inactive ──────────────────────────────────────────────

    @Test
    @DisplayName("verify returns active=true for newly registered citizen")
    void verify_newCitizen_active() {
        String nin = registerAndGetNin("Gurban", "Gurbanov");

        ResponseEntity<ApiResponse<CitizenVerifyResponse>> resp =
                restTemplate.exchange("/api/v1/citizens/{nin}/verify",
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders()),
                        new ParameterizedTypeReference<>() {},
                        nin);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getData().isExists()).isTrue();
        assertThat(resp.getBody().getData().isActive()).isTrue();
    }

    @Test
    @DisplayName("verify returns active=false after status changed to DECEASED")
    void verify_deceasedCitizen_inactive() {
        String nin = registerAndGetNin("Oraz", "Oraz");

        StatusUpdateRequest statusReq = new StatusUpdateRequest();
        statusReq.setStatus(CitizenStatus.DECEASED);
        restTemplate.exchange("/api/v1/citizens/{nin}/status",
                HttpMethod.PATCH,
                new HttpEntity<>(statusReq, authHeaders()),
                new ParameterizedTypeReference<>() {},
                nin);

        ResponseEntity<ApiResponse<CitizenVerifyResponse>> resp =
                restTemplate.exchange("/api/v1/citizens/{nin}/verify",
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders()),
                        new ParameterizedTypeReference<>() {},
                        nin);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getData().isExists()).isTrue();
        assertThat(resp.getBody().getData().isActive()).isFalse();
    }

    // ── 404 for unknown NIN ───────────────────────────────────────────────────

    @Test
    @DisplayName("getByNationalId returns 404 for unknown NIN")
    void getByNationalId_unknown_returns404() {
        ResponseEntity<Object> resp =
                restTemplate.exchange("/api/v1/citizens/{nin}",
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders()),
                        Object.class,
                        "00000000000");

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── 400 for invalid body ──────────────────────────────────────────────────

    @Test
    @DisplayName("register returns 400 for missing required fields")
    void register_invalidBody_returns400() {
        Map<String, Object> badReq = Map.of("firstName", "");

        ResponseEntity<Object> resp =
                restTemplate.exchange("/api/v1/citizens",
                        HttpMethod.POST,
                        new HttpEntity<>(badReq, authHeaders()),
                        Object.class);

        assertThat(resp.getStatusCode()).isIn(HttpStatus.BAD_REQUEST);
    }

    // ── Search by name ────────────────────────────────────────────────────────

    @Test
    @DisplayName("search returns citizen when queried by first name")
    void search_byName_returnsMatch() {
        registerAndGetNin("Ayna", "Aydogdyyewa");

        ResponseEntity<ApiResponse<PageResponse<CitizenResponse>>> resp =
                restTemplate.exchange("/api/v1/citizens/search?name=Ayna",
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders()),
                        new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getData().getContent())
                .anyMatch(c -> "Ayna".equals(c.getFirstName()));
    }

    // ── Sequential registrations get unique NIDs ──────────────────────────────

    @Test
    @DisplayName("two citizens with same DOB get distinct NINs")
    void sequentialRegistrations_uniqueNins() {
        String nin1 = registerAndGetNin("Akja", "Berdiyew");
        String nin2 = registerAndGetNin("Mekan", "Berdiyew");

        assertThat(nin1).isNotEqualTo(nin2);
        assertThat(nin1).matches("\\d{11}");
        assertThat(nin2).matches("\\d{11}");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String registerAndGetNin(String firstName, String lastName) {
        ResponseEntity<ApiResponse<CitizenResponse>> resp =
                restTemplate.exchange("/api/v1/citizens",
                        HttpMethod.POST,
                        new HttpEntity<>(buildCreateRequest(firstName, lastName), authHeaders()),
                        new ParameterizedTypeReference<>() {});
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getData().getNationalId();
    }

    private CitizenCreateRequest buildCreateRequest(String firstName, String lastName) {
        CitizenCreateRequest req = new CitizenCreateRequest();
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setDateOfBirth(LocalDate.of(2001, 1, 15));
        req.setGender(Gender.MALE);
        return req;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = jsonHeaders();
        h.setBearerAuth(adminToken);
        return h;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
