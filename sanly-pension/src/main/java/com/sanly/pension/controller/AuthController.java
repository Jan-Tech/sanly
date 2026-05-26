package com.sanly.pension.controller;

import com.sanly.pension.config.JwtTokenProvider;
import com.sanly.pension.config.UserDetailsImpl;
import com.sanly.pension.dto.request.LoginRequest;
import com.sanly.pension.dto.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pension/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(new AuthResponse(jwtProvider.generateToken(ud), ud.getUsername(), ud.getRole()));
    }
}
