package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.response.AuthResponse;
import com.testing.traningproject.model.dto.request.LoginRequest;
import com.testing.traningproject.model.dto.request.RegisterRequest;
import com.testing.traningproject.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Helper method to add authentication cookies
     *
     * @param response HttpServletResponse
     * @param accessToken JWT access token
     * @param refreshToken JWT refresh token
     */
    private void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token Cookie (24 hours)
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true); // Prevent JavaScript access (XSS protection)
        accessTokenCookie.setSecure(false);  // Set to true in production with HTTPS
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(24 * 60 * 60); // 24 hours in seconds
        response.addCookie(accessTokenCookie);

        // Refresh Token Cookie (7 days)
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true); // Prevent JavaScript access
        refreshTokenCookie.setSecure(false);  // Set to true in production with HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days in seconds
        response.addCookie(refreshTokenCookie);

        log.info("Authentication cookies added successfully");
    }
}

