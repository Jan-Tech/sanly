package com.sanly.business.controller;

import com.sanly.business.config.JwtTokenProvider;
import com.sanly.business.config.UserDetailsImpl;
import com.sanly.business.dto.ApiResponse;
import com.sanly.business.dto.AuthResponse;
import com.sanly.business.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authManager, JwtTokenProvider jwtTokenProvider) {
        this.authManager = authManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
        String token = jwtTokenProvider.generate(auth);
        var roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(ApiResponse.ok("Login successful", new AuthResponse(token, user.getUsername(), roles)));
    }
}
