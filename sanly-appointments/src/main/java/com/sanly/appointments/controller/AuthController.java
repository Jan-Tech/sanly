package com.sanly.appointments.controller;

import com.sanly.appointments.config.JwtTokenProvider;
import com.sanly.appointments.config.UserDetailsImpl;
import com.sanly.appointments.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/appointments/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Officer and admin authentication")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;

    @Data
    static class LoginRequest {
        @NotBlank String username;
        @NotBlank String password;
    }

    @PostMapping("/login")
    @Operation(summary = "Officer / admin login — returns Bearer JWT")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        String token = tokenProvider.generateToken(principal);
        Set<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        return ApiResponse.<Map<String, Object>>builder()
                .success(true).message("Login successful")
                .data(Map.of("token", token, "tokenType", "Bearer",
                        "username", principal.getUsername(), "roles", roles,
                        "officeCode", principal.getOfficeCode() != null ? principal.getOfficeCode() : ""))
                .build();
    }
}
