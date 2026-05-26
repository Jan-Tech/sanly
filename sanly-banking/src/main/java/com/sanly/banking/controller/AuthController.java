package com.sanly.banking.controller;

import com.sanly.banking.config.JwtTokenProvider;
import com.sanly.banking.config.OfficerUserDetails;
import com.sanly.banking.dto.request.LoginRequest;
import com.sanly.banking.dto.response.ApiResponse;
import com.sanly.banking.dto.response.JwtResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/banking/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Banking officer authentication")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Officer login", description = "Authenticate a banking officer and receive a JWT token")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        OfficerUserDetails officer = (OfficerUserDetails) auth.getPrincipal();
        String role = officer.getAuthorities().iterator().next().getAuthority();
        String token = tokenProvider.generateTokenForOfficer(officer.getUsername(), role);

        JwtResponse response = JwtResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationMs())
                .username(officer.getUsername())
                .role(role)
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }
}
