package com.testing.traningproject.service;

import com.testing.traningproject.exception.BadRequestException;
import com.testing.traningproject.exception.DuplicateResourceException;
import com.testing.traningproject.exception.ResourceNotFoundException;
import com.testing.traningproject.exception.UnauthorizedException;
import com.testing.traningproject.model.dto.AuthResponse;
import com.testing.traningproject.model.dto.LoginRequest;
import com.testing.traningproject.model.dto.RegisterRequest;
import com.testing.traningproject.model.dto.UserResponse;
import com.testing.traningproject.model.entity.Role;
import com.testing.traningproject.model.entity.User;
import com.testing.traningproject.model.enums.AccountStatus;
import com.testing.traningproject.model.enums.RoleName;
import com.testing.traningproject.repository.RoleRepository;
import com.testing.traningproject.repository.UserRepository;
import com.testing.traningproject.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles user registration and login
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate email doesn't already exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Get role from database
        // getRole() method form @Data in registerRequest as ready method
       // RoleName from enums , so check if in request body role found in enums
        RoleName roleName;
        try {
            roleName = RoleName.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }

        // Role entity  check if found in it or no
        Role role = roleRepository.findByName(roleName)
                 .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        // Set account status
        // SERVICE_PROVIDER needs admin approval, CUSTOMER is immediately active
        AccountStatus accountStatus = roleName == RoleName.SERVICE_PROVIDER
                ? AccountStatus.PENDING_APPROVAL
                : AccountStatus.ACTIVE;

        // add the role user determine when register
        // build set (set not allow in duplicate) for may the user can be customer and provider
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .accountStatus(accountStatus)
                .bio(request.getBio())
                .professionalTitle(request.getProfessionalTitle())
                .certificateUrl(request.getCertificateUrl())
                .roles(roles)
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);

        // Generate JWT token
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        savedUser.getEmail(),
                        savedUser.getPasswordHash(),
                        savedUser.getRoles().stream()
                                .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                                .collect(Collectors.toList())
                );

        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Build and return response
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .user(mapToUserResponse(savedUser))
                .build();
    }

    /**
     * Login user
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        // AuthenticationManager & UsernamePasswordAuthenticationToken ( Authentication object)
        // from spring security framework
        // use the CustomUserDetailsService to check in securityConfig
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // Load user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Check account status
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new UnauthorizedException("Account is suspended. Please contact support.");
        }

        if (user.getAccountStatus() == AccountStatus.PENDING_APPROVAL) {
            throw new UnauthorizedException("Account is pending admin approval. Please wait for approval.");
        }

        if (user.getAccountStatus() == AccountStatus.REJECTED) {
            throw new UnauthorizedException("Account registration was rejected. Please contact support.");
        }

        // if active : Generate JWT token
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPasswordHash(),
                        user.getRoles().stream()
                                .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                                .collect(Collectors.toList())
                );

        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Build and return response
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .user(mapToUserResponse(user))
                .build();
    }

// to map the data send from BE to user with the UserResponse we created it
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profilePictureUrl(user.getProfilePictureUrl())
                .bio(user.getBio())
                .professionalTitle(user.getProfessionalTitle())
                .certificateUrl(user.getCertificateUrl())
                .accountStatus(user.getAccountStatus())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

