package com.sanly.business.dto;

public record AuthResponse(String token, String username, java.util.Set<String> roles) {}
