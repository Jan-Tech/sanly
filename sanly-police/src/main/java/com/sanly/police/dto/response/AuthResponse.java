package com.sanly.police.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data @Builder
public class AuthResponse {
    private String token, tokenType;
    private long expiresIn;
    private String username;
    private Set<String> roles;
}
