package com.testing.traningproject.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration request
 * Validates all input fields before creating a new user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Phone number must be valid (10-20 digits)")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "CUSTOMER|SERVICE_PROVIDER", message = "Role must be either CUSTOMER or SERVICE_PROVIDER")
    private String role; // CUSTOMER or SERVICE_PROVIDER (not ADMIN - admins created manually)

    // Service Provider specific fields (optional for CUSTOMER)
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @Size(max = 200, message = "Professional title must not exceed 200 characters")
    private String professionalTitle;

    @Size(max = 500, message = "Certificate URL must not exceed 500 characters")
    private String certificateUrl;
}

