package com.sanly.registry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanly.registry.audit.AuditService;
import com.sanly.registry.config.JwtTokenProvider;
import com.sanly.registry.dto.request.CitizenCreateRequest;
import com.sanly.registry.dto.request.StatusUpdateRequest;
import com.sanly.registry.dto.response.CitizenResponse;
import com.sanly.registry.dto.response.CitizenVerifyResponse;
import com.sanly.registry.dto.response.PageResponse;
import com.sanly.registry.entity.CitizenStatus;
import com.sanly.registry.entity.Gender;
import com.sanly.registry.exception.CitizenNotFoundException;
import com.sanly.registry.repository.ActiveSessionRepository;
import com.sanly.registry.service.CitizenService;
import com.sanly.registry.service.SecurityService;
import com.sanly.registry.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitizenController.class)
@ActiveProfiles("test")
@DisplayName("CitizenController")
class CitizenControllerTest {

    private static final String NIN = "50101150010";

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  objectMapper;

    @MockBean CitizenService          citizenService;
    @MockBean AuditService            auditService;
    @MockBean JwtTokenProvider        jwtTokenProvider;
    @MockBean UserDetailsServiceImpl  userDetailsService;
    @MockBean ActiveSessionRepository activeSessionRepository;
    @MockBean SecurityService         securityService;

    // ── POST /api/v1/citizens ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("register returns 201 with NIN for ADMIN")
    void register_asAdmin_returns201() throws Exception {
        when(citizenService.register(any())).thenReturn(buildResponse());
        doNothing().when(auditService).log(any(), any(), any());

        mockMvc.perform(post("/api/v1/citizens")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nationalId").value(NIN));
    }

    @Test
    @WithMockUser(roles = "INSTITUTION")
    @DisplayName("register returns 403 for INSTITUTION role")
    void register_asInstitution_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/citizens")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/v1/citizens/{nationalId} ─────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("getByNationalId returns 200 for known NIN")
    void getByNationalId_found_returns200() throws Exception {
        when(citizenService.getByNationalId(NIN)).thenReturn(buildResponse());
        doNothing().when(auditService).log(any(), any(), any());

        mockMvc.perform(get("/api/v1/citizens/{nin}", NIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nationalId").value(NIN))
                .andExpect(jsonPath("$.data.firstName").value("Merdan"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("getByNationalId returns 404 for unknown NIN")
    void getByNationalId_notFound_returns404() throws Exception {
        when(citizenService.getByNationalId(NIN)).thenThrow(new CitizenNotFoundException(NIN));
        doNothing().when(auditService).log(any(), any(), any());

        mockMvc.perform(get("/api/v1/citizens/{nin}", NIN))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/v1/citizens/{nationalId}/verify ──────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("verify returns 200 with exists=true and active=true for ACTIVE citizen")
    void verify_active_returns200() throws Exception {
        when(citizenService.verify(NIN))
                .thenReturn(CitizenVerifyResponse.builder()
                        .nationalId(NIN).exists(true).active(true)
                        .status(CitizenStatus.ACTIVE).build());
        doNothing().when(auditService).log(any(), any(), any());

        mockMvc.perform(get("/api/v1/citizens/{nin}/verify", NIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exists").value(true))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    // ── PATCH /api/v1/citizens/{nationalId}/status ────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("updateStatus returns 200 with new status")
    void updateStatus_returns200() throws Exception {
        CitizenResponse deceasedResponse = CitizenResponse.builder()
                .nationalId(NIN).firstName("Merdan").lastName("Atayew")
                .status(CitizenStatus.DECEASED).build();
        when(citizenService.updateStatus(eq(NIN), any())).thenReturn(deceasedResponse);
        doNothing().when(auditService).log(any(), any(), any());

        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(CitizenStatus.DECEASED);

        mockMvc.perform(patch("/api/v1/citizens/{nin}/status", NIN)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DECEASED"));
    }

    // ── GET /api/v1/citizens/search ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("search returns 200 with paged results")
    void search_returns200() throws Exception {
        PageResponse<CitizenResponse> page = PageResponse.<CitizenResponse>builder()
                .content(List.of(buildResponse()))
                .totalElements(1L).totalPages(1).page(0).size(20).build();
        when(citizenService.search(any(), any(), any(), anyInt(), anyInt())).thenReturn(page);
        doNothing().when(auditService).log(any(), any(), any());

        mockMvc.perform(get("/api/v1/citizens/search").param("name", "Merdan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].nationalId").value(NIN));
    }

    @Test
    @DisplayName("unauthenticated request returns 401")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/citizens/{nin}", NIN))
                .andExpect(status().isUnauthorized());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CitizenCreateRequest buildCreateRequest() {
        CitizenCreateRequest req = new CitizenCreateRequest();
        req.setFirstName("Merdan");
        req.setLastName("Atayew");
        req.setDateOfBirth(LocalDate.of(2001, 1, 15));
        req.setGender(Gender.MALE);
        return req;
    }

    private CitizenResponse buildResponse() {
        return CitizenResponse.builder()
                .nationalId(NIN)
                .firstName("Merdan")
                .lastName("Atayew")
                .dateOfBirth(LocalDate.of(2001, 1, 15))
                .gender(Gender.MALE)
                .status(CitizenStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
