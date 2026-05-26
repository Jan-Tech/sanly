package com.sanly.signature.controller;

import com.sanly.signature.config.JwtTokenProvider;
import com.sanly.signature.config.UserDetailsImpl;
import com.sanly.signature.dto.request.AuthRequest;
import com.sanly.signature.dto.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/signature/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody AuthRequest request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(user);
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> tokenData = Map.of(
                "token", token,
                "tokenType", "Bearer",
                "expiresIn", jwtTokenProvider.getExpirationMs(),
                "username", user.getUsername(),
                "roles", roles
        );

        return ResponseEntity.ok(ApiResponse.ok("Login successful", tokenData));
    }
}
