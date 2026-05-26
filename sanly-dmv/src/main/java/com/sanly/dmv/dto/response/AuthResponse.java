package com.sanly.dmv.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data @Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private long expiresIn;
    private String username;
    private Set<String> roles;
    private Long officerId;
}
