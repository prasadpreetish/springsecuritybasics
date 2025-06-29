package com.basicsecurity.payload;

// No need for Lombok annotations
public record RegisterDto(
        String name,
        String username,
        String email,
        String password
) {
}