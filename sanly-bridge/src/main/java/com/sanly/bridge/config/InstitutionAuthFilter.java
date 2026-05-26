package com.sanly.bridge.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sanly.bridge.dto.response.ErrorResponse;
import com.sanly.bridge.entity.Institution;
import com.sanly.bridge.entity.InstitutionStatus;
import com.sanly.bridge.repository.InstitutionRepository;
import com.sanly.bridge.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Authenticates institution-to-bridge calls using two custom headers:
 * <ul>
 *   <li>{@code X-Institution-Code} — the institution's unique code (e.g. INST_MEDICAL)</li>
 *   <li>{@code X-Institution-Key}  — the raw API key (BCrypt-verified against stored hash)</li>
 * </ul>
 *
 * Only runs for {@code /api/v1/exchange/**} paths.
 * On success, sets an {@link InstitutionAuthentication} in SecurityContextHolder.
 * On failure, returns 401/403 immediately without invoking the filter chain.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InstitutionAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_CODE = "X-Institution-Code";
    private static final String HEADER_KEY  = "X-Institution-Key";
    private static final String EXCHANGE_PATH = "/api/v1/exchange";

    private final InstitutionRepository institutionRepository;
    private final ApiKeyService         apiKeyService;
    private final ObjectMapper          objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(EXCHANGE_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {

        String code = request.getHeader(HEADER_CODE);
        String key  = request.getHeader(HEADER_KEY);

        if (!StringUtils.hasText(code) || !StringUtils.hasText(key)) {
            sendError(response, request, 401, "Missing X-Institution-Code or X-Institution-Key headers");
            return;
        }

        Optional<Institution> opt = institutionRepository.findByInstitutionCode(code);

        // Identical error for "not found" and "wrong key" — prevent enumeration
        if (opt.isEmpty() || !apiKeyService.verify(key, opt.get().getHashedApiKey())) {
            log.warn("Failed authentication attempt for institution code: {}", code);
            sendError(response, request, 401, "Invalid institution credentials");
            return;
        }

        Institution institution = opt.get();
        if (institution.getStatus() == InstitutionStatus.SUSPENDED) {
            log.warn("Suspended institution attempted access: {}", code);
            sendError(response, request, 403, "Institution " + code + " is suspended");
            return;
        }
        if (institution.getStatus() == InstitutionStatus.PENDING) {
            sendError(response, request, 403, "Institution " + code + " is pending activation");
            return;
        }

        SecurityContextHolder.getContext()
                .setAuthentication(new InstitutionAuthentication(code));

        chain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, HttpServletRequest request,
                           int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.builder()
                .status(status)
                .error(status == 401 ? "Unauthorized" : "Forbidden")
                .message(message)
                .path(request.getRequestURI())
                .build();
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
