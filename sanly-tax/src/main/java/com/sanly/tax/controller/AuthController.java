package com.sanly.tax.controller;

import com.sanly.tax.config.JwtTokenProvider;
import com.sanly.tax.config.UserDetailsImpl;
import com.sanly.tax.dto.request.AuthRequest;
import com.sanly.tax.dto.response.ApiResponse;
import com.sanly.tax.dto.response.AuthResponse;
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
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserDetailsImpl p = (UserDetailsImpl) auth.getPrincipal();
        Set<String> roles = p.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        return ApiResponse.<AuthResponse>builder().success(true).message("Login successful")
                .data(AuthResponse.builder().token(tokenProvider.generateToken(p)).tokenType("Bearer")
                        .expiresIn(tokenProvider.getExpirationMs()).username(p.getUsername()).roles(roles).build()).build();
    }
}
