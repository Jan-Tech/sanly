package com.sanly.education.controller;

import com.sanly.education.config.JwtTokenProvider;
import com.sanly.education.dto.request.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/education/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authManager, JwtTokenProvider jwtTokenProvider) {
        this.authManager = authManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        return ResponseEntity.ok(Map.of("token", jwtTokenProvider.generate(auth)));
    }
}
