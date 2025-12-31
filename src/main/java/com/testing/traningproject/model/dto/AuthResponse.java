package com.testing.traningproject.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response
 * Contains JWT token and user information
 * Returned after successful login or registration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token; // JWT access token
    private String refreshToken; // JWT refresh token (optional for later)
    private String tokenType = "Bearer"; // Always "Bearer"
    private Long expiresIn; // Token expiration time in milliseconds
    private UserResponse user; // User information (without password!)
}

