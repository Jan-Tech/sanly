package com.sanly.bridge.controller;

import com.sanly.bridge.config.JwtTokenProvider;
import com.sanly.bridge.config.UserDetailsImpl;
import com.sanly.bridge.dto.request.AuthRequest;
import com.sanly.bridge.dto.response.ApiResponse;
import com.sanly.bridge.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Obtain admin JWT token")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider      tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Login and receive a Bearer JWT for admin endpoints")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        String token = tokenProvider.generateToken(principal);

        Set<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(AuthResponse.builder()
                        .token(token).tokenType("Bearer")
                        .expiresIn(tokenProvider.getExpirationMs())
                        .username(principal.getUsername())
                        .roles(roles)
                        .build())
                .build();
    }
}
