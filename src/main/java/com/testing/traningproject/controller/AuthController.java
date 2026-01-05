package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.response.AuthResponse;
import com.testing.traningproject.model.dto.request.LoginRequest;
import com.testing.traningproject.model.dto.request.RegisterRequest;
import com.testing.traningproject.security.JwtService;
import com.testing.traningproject.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication Controller
 * Handles registration, login, and logout endpoints with Cookie-based JWT storage
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * Register a new user
     * Stores JWT token in HttpOnly Cookie for security
     *
     * @param request RegisterRequest with user details
     * @param response HttpServletResponse to add cookies
     * @return AuthResponse with user info (token stored in cookie)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        log.info("Registration request for email: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);

        // Only add cookies if tokens are generated (not for PENDING_APPROVAL users)
        if (authResponse.getToken() != null) {
            addAuthCookies(response, authResponse.getToken(), authResponse.getRefreshToken());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    /**
     * Login user
     * Stores JWT token in HttpOnly Cookie for security
     *
     * @param request LoginRequest with email and password
     * @param response HttpServletResponse to add cookies
     * @return AuthResponse with user info (token stored in cookie)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        log.info("Login request for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);

        // Add tokens to cookies
        addAuthCookies(response, authResponse.getToken(), authResponse.getRefreshToken());

        return ResponseEntity.ok(authResponse);
    }

    /**
     * Logout user
     * Clears authentication cookies
     *
     * @param response HttpServletResponse to clear cookies
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        log.info("Logout request received");

        // Clear access token cookie
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false); // Set to true in production with HTTPS
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0); // Delete immediately
        response.addCookie(accessTokenCookie);

        // Clear refresh token cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // Set to true in production with HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // Delete immediately
        response.addCookie(refreshTokenCookie);

        log.info("User logged out successfully");
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Refresh Access Token using Refresh Token from Cookie
     * Called when access token expires
     *
     * @param refreshToken Refresh token from cookie
     * @param response HttpServletResponse to update access token cookie
     * @return Success message with new token expiry time
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        log.info("Token refresh request received");

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Refresh token not found in cookies");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token not found. Please login again."));
        }

        try {
            // Validate refresh token
            if (!jwtService.validateToken(refreshToken)) {
                log.warn("Invalid or expired refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired refresh token. Please login again."));
            }

            // Extract email from refresh token
            String email = jwtService.extractUsername(refreshToken);

            // Generate new access token
            String newAccessToken = jwtService.generateToken(email);

            // Update access token cookie only
            Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false); // Set to true in production with HTTPS
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days (matches JWT expiration)
            response.addCookie(accessTokenCookie);

            log.info("Access token refreshed successfully for user: {}", email);

            return ResponseEntity.ok(Map.of(
                    "message", "Token refreshed successfully",
                    "expiresIn", 604800000 // 7 days in milliseconds
            ));

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token refresh failed. Please login again."));
        }
    }

    /**
     * Helper method to add authentication cookies
     *
     * @param response HttpServletResponse
     * @param accessToken JWT access token
     * @param refreshToken JWT refresh token
     */
    private void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token Cookie (7 days - FOR DEVELOPMENT)
        // In production, should be 24 hours
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true); // Prevent JavaScript access (XSS protection)
        accessTokenCookie.setSecure(false);  // Set to true in production with HTTPS
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days (matches JWT expiration)
        response.addCookie(accessTokenCookie);

        // Refresh Token Cookie (30 days)
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true); // Prevent JavaScript access
        refreshTokenCookie.setSecure(false);  // Set to true in production with HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days (matches refresh token expiration)
        response.addCookie(refreshTokenCookie);

        log.info("Authentication cookies added successfully (Access: 7d, Refresh: 30d)");
    }
}
