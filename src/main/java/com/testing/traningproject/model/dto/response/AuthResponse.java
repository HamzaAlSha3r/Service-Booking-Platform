package com.testing.traningproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response
 *
 * NOTE: JWT tokens are stored in HttpOnly Cookies for security
 * - accessToken: 24 hours expiration
 * - refreshToken: 7 days expiration
 *
 * Tokens in response body are for backward compatibility only
 * Frontend should NOT store these tokens manually
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token; // JWT access token (also stored in Cookie)
    private String refreshToken; // JWT refresh token (also stored in Cookie)
    private String tokenType; // Always "Bearer"
    private Long expiresIn; // Token expiration time in milliseconds
    private UserResponse user; // User information (without password!)
    private String message; // Optional message (e.g., "Pending approval")
}

