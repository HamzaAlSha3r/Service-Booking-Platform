package com.testing.traningproject.model.dto.response;

import com.testing.traningproject.model.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for user response
 * NEVER expose the password or sensitive data!
 * Used in API responses to send user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profilePictureUrl;

    // Service Provider specific fields
    private String bio;
    private String professionalTitle;
    private String certificateUrl;
    private AccountStatus accountStatus;

    // Roles
    private Set<String> roles; // ["CUSTOMER", "SERVICE_PROVIDER"]

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

