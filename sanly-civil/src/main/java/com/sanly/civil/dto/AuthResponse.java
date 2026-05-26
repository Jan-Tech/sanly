package com.sanly.civil.dto;

public record AuthResponse(String token, String username, java.util.Set<String> roles) {}
