package com.basicsecurity.payload;

// No need for Lombok annotations like @Data, @NoArgsConstructor, @AllArgsConstructor
// Records automatically provide:
// - A canonical constructor (all-argument constructor)
// - Accessor methods (like getters, e.g., loginDto.usernameOrEmail())
// - equals(), hashCode(), and toString() implementations
public record LoginDto(String usernameOrEmail, String password) {
    // You can add custom methods here if needed, but for a simple DTO, often not.
}