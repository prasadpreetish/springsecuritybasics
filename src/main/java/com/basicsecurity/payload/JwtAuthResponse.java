package com.basicsecurity.payload;

// No need for Lombok annotations.
public record JwtAuthResponse(String accessToken, String tokenType) {

    // This is a "compact constructor" for records.
    // It's useful for validation or setting default values.
    // The actual field assignment (this.accessToken = accessToken) is implicit.
    public JwtAuthResponse {
        // Ensure tokenType defaults to "Bearer" if not explicitly passed
        if (tokenType == null || tokenType.isEmpty()) {
            tokenType = "Bearer";
        }
    }

    // You can also add an overloaded constructor if you want to allow creating
    // a JwtAuthResponse by only providing accessToken, with tokenType defaulting.
    public JwtAuthResponse(String accessToken) {
        this(accessToken, "Bearer"); // Calls the canonical constructor
    }
}