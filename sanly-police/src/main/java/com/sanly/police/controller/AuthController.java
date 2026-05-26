package com.sanly.police.controller;

import com.sanly.police.config.JwtTokenProvider;
import com.sanly.police.config.UserDetailsImpl;
import com.sanly.police.dto.request.AuthRequest;
import com.sanly.police.dto.response.ApiResponse;
import com.sanly.police.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        String token = tokenProvider.generateToken(principal);
        Set<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        return ApiResponse.<AuthResponse>builder().success(true).message("Login successful")
                .data(AuthResponse.builder().token(token).tokenType("Bearer")
                        .expiresIn(tokenProvider.getExpirationMs())
                        .username(principal.getUsername()).roles(roles).build()).build();
    }
}
