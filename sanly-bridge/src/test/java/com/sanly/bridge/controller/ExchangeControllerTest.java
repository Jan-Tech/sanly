package com.sanly.bridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanly.bridge.config.JwtTokenProvider;
import com.sanly.bridge.dto.request.ExchangePublishRequest;
import com.sanly.bridge.dto.request.ExchangeQueryRequest;
import com.sanly.bridge.dto.response.ExchangeResponse;
import com.sanly.bridge.dto.response.PublishedDataResponse;
import com.sanly.bridge.entity.*;
import com.sanly.bridge.repository.InstitutionRepository;
import com.sanly.bridge.service.ApiKeyService;
import com.sanly.bridge.service.ExchangeService;
import com.sanly.bridge.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for ExchangeController.
 *
 * Authentication is handled by InstitutionAuthFilter, which validates
 * X-Institution-Code + X-Institution-Key headers against the repository.
 * Tests that require auth mock the repository/service and provide both headers.
 */
@WebMvcTest(ExchangeController.class)
@ActiveProfiles("test")
@DisplayName("ExchangeController")
class ExchangeControllerTest {

    private static final String INST_CODE    = "INST_POLICE";
    private static final String API_KEY      = "sk_bridge_testkey";
    private static final String NIN          = "50101150010";

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ExchangeService        exchangeService;
    @MockBean JwtTokenProvider       jwtTokenProvider;
    @MockBean UserDetailsServiceImpl userDetailsService;
    @MockBean InstitutionRepository  institutionRepository;
    @MockBean ApiKeyService          apiKeyService;

    @BeforeEach
    void stubInstitutionAuth() {
        Institution institution = Institution.builder()
                .institutionCode(INST_CODE)
                .name("Police Department")
                .hashedApiKey("hashed_" + API_KEY)
                .status(InstitutionStatus.ACTIVE)
                .build();
        when(institutionRepository.findByInstitutionCode(INST_CODE))
                .thenReturn(Optional.of(institution));
        when(apiKeyService.verify(eq(API_KEY), anyString())).thenReturn(true);
    }

    // ── POST /api/v1/exchange/query ───────────────────────────────────────────

    @Test
    @DisplayName("query returns 200 with authenticated institution headers")
    void query_authenticated_returns200() throws Exception {
        ExchangeResponse body = buildExchangeResponse(ExchangeResult.SUCCESS, OperationType.QUERY);
        when(exchangeService.query(eq(INST_CODE), any())).thenReturn(body);

        mockMvc.perform(post("/api/v1/exchange/query")
                        .header("X-Institution-Code", INST_CODE)
                        .header("X-Institution-Key",  API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildQueryRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.result").value("SUCCESS"));
    }

    @Test
    @DisplayName("query returns 401 when institution headers are absent")
    void query_noHeaders_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/exchange/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildQueryRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("query returns 401 when API key is wrong")
    void query_wrongApiKey_returns401() throws Exception {
        when(apiKeyService.verify(eq("wrong-key"), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/v1/exchange/query")
                        .header("X-Institution-Code", INST_CODE)
                        .header("X-Institution-Key",  "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildQueryRequest())))
                .andExpect(status().isUnauthorized());
    }

    // ── POST /api/v1/exchange/publish ─────────────────────────────────────────

    @Test
    @DisplayName("publish returns 201 with authenticated institution headers")
    void publish_authenticated_returns201() throws Exception {
        ExchangeResponse body = buildExchangeResponse(ExchangeResult.SUCCESS, OperationType.PUBLISH);
        when(exchangeService.publish(eq(INST_CODE), any())).thenReturn(body);

        mockMvc.perform(post("/api/v1/exchange/publish")
                        .header("X-Institution-Code", INST_CODE)
                        .header("X-Institution-Key",  API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildPublishRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.operationType").value("PUBLISH"));
    }

    // ── GET /api/v1/exchange/data/{nationalId}/{dataType} ─────────────────────

    @Test
    @DisplayName("getData returns 200 with list of published records")
    void getData_authenticated_returns200() throws Exception {
        PublishedDataResponse record = PublishedDataResponse.builder()
                .nationalId(NIN)
                .dataType(DataType.TAX_STATUS)
                .publisherCode(INST_CODE)
                .build();
        when(exchangeService.getData(eq(INST_CODE), eq(NIN), eq(DataType.TAX_STATUS),
                any(), any(), any())).thenReturn(List.of(record));

        mockMvc.perform(get("/api/v1/exchange/data/{nin}/{type}", NIN, DataType.TAX_STATUS)
                        .header("X-Institution-Code", INST_CODE)
                        .header("X-Institution-Key",  API_KEY)
                        .param("purposeCode", QueryPurpose.CRIMINAL_INVESTIGATION.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nationalId").value(NIN))
                .andExpect(jsonPath("$.data[0].dataType").value("TAX_STATUS"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ExchangeQueryRequest buildQueryRequest() {
        ExchangeQueryRequest req = new ExchangeQueryRequest();
        req.setNationalId(NIN);
        req.setDataType(DataType.TAX_STATUS);
        req.setPurposeCode(QueryPurpose.CRIMINAL_INVESTIGATION);
        return req;
    }

    private ExchangePublishRequest buildPublishRequest() {
        ExchangePublishRequest req = new ExchangePublishRequest();
        req.setNationalId(NIN);
        req.setDataType(DataType.TAX_STATUS);
        return req;
    }

    private ExchangeResponse buildExchangeResponse(ExchangeResult result, OperationType type) {
        return ExchangeResponse.builder()
                .exchangeId(1L)
                .requestingCode(INST_CODE)
                .nationalId(NIN)
                .dataType(DataType.TAX_STATUS)
                .operationType(type)
                .result(result)
                .exchangedAt(LocalDateTime.now())
                .build();
    }
}
