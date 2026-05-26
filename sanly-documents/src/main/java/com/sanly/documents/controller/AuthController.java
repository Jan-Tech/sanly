package com.sanly.documents.controller;

import com.sanly.documents.config.JwtTokenProvider;
import com.sanly.documents.config.UserDetailsImpl;
import com.sanly.documents.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(
            @RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(user);

        return ResponseEntity.ok(ApiResponse.ok(Map.of("token", token, "role", user.getRole())));
    }
}
