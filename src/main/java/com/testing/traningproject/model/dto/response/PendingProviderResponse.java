package com.testing.traningproject.model.dto.response;

import com.testing.traningproject.model.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for pending service provider
 * Returns provider details for admin approval
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingProviderResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String bio;
    private String professionalTitle;
    private String certificateUrl;
    private AccountStatus accountStatus;
    private Set<String> roles;
    private LocalDateTime createdAt;
}

