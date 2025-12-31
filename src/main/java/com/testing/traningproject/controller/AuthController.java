package com.testing.traningproject.controller;

import com.testing.traningproject.model.dto.AuthResponse;
import com.testing.traningproject.model.dto.LoginRequest;
import com.testing.traningproject.model.dto.RegisterRequest;
import com.testing.traningproject.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles registration and login endpoints
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     * POST /api/auth/register
     *
     * @param request RegisterRequest with user details
     * @return AuthResponse with JWT token and user info
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user
     * POST /api/auth/login
     *
     * @param request LoginRequest with email and password
     * @return AuthResponse with JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

