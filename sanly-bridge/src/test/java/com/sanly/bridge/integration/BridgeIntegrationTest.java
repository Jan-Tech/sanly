package com.sanly.bridge.integration;

import com.sanly.bridge.AbstractIntegrationTest;
import com.sanly.bridge.client.NotificationClient;
import com.sanly.bridge.dto.request.*;
import com.sanly.bridge.dto.response.*;
import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.ExchangeResult;
import com.sanly.bridge.entity.QueryPurpose;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for sanly-bridge.
 *
 * {@code @TestInstance(PER_CLASS)} lets @BeforeAll be non-static so Spring beans
 * are accessible. The institutions and admin token are set up once for the entire
 * test class to avoid re-registration conflicts (institution codes are unique).
 *
 * Scenario exercised across tests:
 *   - Admin logs in and gets a JWT
 *   - Admin registers INST_POLICE (publishableTypes=[CRIMINAL_RECORD])
 *   - Admin registers INST_TAX
 *   - Permission granted → publish → query → SUCCESS
 *   - Permission revoked → query → DENIED
 *   - Query without any permission → DENIED
 */
@DisplayName("Bridge integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BridgeIntegrationTest extends AbstractIntegrationTest {

    private static final String INST_POLICE = "INST_POLICE_IT";
    private static final String INST_TAX    = "INST_TAX_IT";
    private static final String NIN         = "50101150010";

    @Autowired TestRestTemplate restTemplate;
    @MockBean  NotificationClient notificationClient;

    private String adminToken;
    private String policeApiKey;
    private String taxApiKey;

    @BeforeAll
    void setupOnce() {
        adminToken   = loginAdmin();
        policeApiKey = registerInstitution(INST_POLICE, Set.of(DataType.CRIMINAL_RECORD));
        taxApiKey    = registerInstitution(INST_TAX, Set.of());
    }

    // ── Publish then query → SUCCESS ─────────────────────────────────────────

    @Test
    @DisplayName("INST_TAX can query CRIMINAL_RECORD published by INST_POLICE when permission exists")
    void publishThenQuery_withPermission_success() {
        Long permId = grantPermission(INST_TAX, INST_POLICE, DataType.CRIMINAL_RECORD);
        try {
            publishRecord(INST_POLICE, policeApiKey, NIN, DataType.CRIMINAL_RECORD);
            ExchangeResponse result = queryRecord(INST_TAX, taxApiKey, NIN, DataType.CRIMINAL_RECORD);
            assertThat(result.getResult()).isEqualTo(ExchangeResult.SUCCESS);
        } finally {
            revokePermission(permId);
        }
    }

    // ── Revoke permission → DENIED ────────────────────────────────────────────

    @Test
    @DisplayName("INST_TAX query is DENIED after permission is revoked")
    void queryAfterRevoke_denied() {
        Long permId = grantPermission(INST_TAX, INST_POLICE, DataType.CRIMINAL_RECORD);
        publishRecord(INST_POLICE, policeApiKey, NIN, DataType.CRIMINAL_RECORD);
        revokePermission(permId);

        ExchangeResponse result = queryRecord(INST_TAX, taxApiKey, NIN, DataType.CRIMINAL_RECORD);
        assertThat(result.getResult()).isEqualTo(ExchangeResult.DENIED);
    }

    // ── No permission → DENIED without ever granting ─────────────────────────

    @Test
    @DisplayName("query without any permission returns DENIED")
    void queryWithoutPermission_denied() {
        publishRecord(INST_POLICE, policeApiKey, NIN, DataType.CRIMINAL_RECORD);

        ExchangeResponse result = queryRecord(INST_TAX, taxApiKey, NIN, DataType.CRIMINAL_RECORD);
        assertThat(result.getResult()).isEqualTo(ExchangeResult.DENIED);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String loginAdmin() {
        AuthRequest creds = new AuthRequest();
        creds.setUsername("admin");
        creds.setPassword("TestAdmin123!");

        ResponseEntity<ApiResponse<AuthResponse>> resp =
                restTemplate.exchange("/api/v1/auth/login",
                        HttpMethod.POST,
                        new HttpEntity<>(creds, jsonHeaders()),
                        new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = resp.getBody().getData().getToken();
        assertThat(token).isNotBlank();
        return token;
    }

    private String registerInstitution(String code, Set<DataType> publishableTypes) {
        InstitutionCreateRequest req = new InstitutionCreateRequest();
        req.setInstitutionCode(code);
        req.setName(code + " Test");
        req.setPublishableTypes(publishableTypes);

        ResponseEntity<ApiResponse<InstitutionKeyResponse>> resp =
                restTemplate.exchange("/api/v1/institutions",
                        HttpMethod.POST,
                        new HttpEntity<>(req, adminAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String key = resp.getBody().getData().getApiKey();
        assertThat(key).isNotBlank();
        return key;
    }

    private Long grantPermission(String requestingCode, String targetCode, DataType dataType) {
        PermissionCreateRequest req = new PermissionCreateRequest();
        req.setRequestingCode(requestingCode);
        req.setTargetCode(targetCode);
        req.setDataType(dataType);

        ResponseEntity<ApiResponse<PermissionResponse>> resp =
                restTemplate.exchange("/api/v1/permissions",
                        HttpMethod.POST,
                        new HttpEntity<>(req, adminAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getData().getId();
    }

    private void revokePermission(Long permId) {
        restTemplate.exchange("/api/v1/permissions/{id}",
                HttpMethod.DELETE,
                new HttpEntity<>(adminAuthHeaders()),
                Void.class,
                permId);
    }

    private void publishRecord(String instCode, String apiKey,
                                String nationalId, DataType dataType) {
        ExchangePublishRequest req = new ExchangePublishRequest();
        req.setNationalId(nationalId);
        req.setDataType(dataType);
        req.setRecordRef("REF-001");

        ResponseEntity<ApiResponse<ExchangeResponse>> resp =
                restTemplate.exchange("/api/v1/exchange/publish",
                        HttpMethod.POST,
                        new HttpEntity<>(req, institutionHeaders(instCode, apiKey)),
                        new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private ExchangeResponse queryRecord(String instCode, String apiKey,
                                          String nationalId, DataType dataType) {
        ExchangeQueryRequest req = new ExchangeQueryRequest();
        req.setNationalId(nationalId);
        req.setDataType(dataType);
        req.setPurposeCode(QueryPurpose.CRIMINAL_INVESTIGATION);
        req.setCaseReference("CASE-IT-001");

        ResponseEntity<ApiResponse<ExchangeResponse>> resp =
                restTemplate.exchange("/api/v1/exchange/query",
                        HttpMethod.POST,
                        new HttpEntity<>(req, institutionHeaders(instCode, apiKey)),
                        new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return resp.getBody().getData();
    }

    private HttpHeaders adminAuthHeaders() {
        HttpHeaders h = jsonHeaders();
        h.setBearerAuth(adminToken);
        return h;
    }

    private HttpHeaders institutionHeaders(String code, String key) {
        HttpHeaders h = jsonHeaders();
        h.set("X-Institution-Code", code);
        h.set("X-Institution-Key",  key);
        return h;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
