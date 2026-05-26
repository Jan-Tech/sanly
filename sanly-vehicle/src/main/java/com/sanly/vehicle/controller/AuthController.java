package com.sanly.vehicle.controller;

import com.sanly.vehicle.config.JwtTokenProvider;
import com.sanly.vehicle.config.UserDetailsImpl;
import com.sanly.vehicle.dto.request.LoginRequest;
import com.sanly.vehicle.dto.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicle/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
        String token = jwtProvider.generateToken(ud);
        return ResponseEntity.ok(new AuthResponse(token, ud.getUsername(), ud.getRole()));
    }
}
