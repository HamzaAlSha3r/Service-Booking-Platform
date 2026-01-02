package com.testing.traningproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {

    private Long id;

    // Provider info
    private Long providerId;
    private String providerName;
    private String providerEmail;

    // Category info
    private Integer categoryId;
    private String categoryName;

    // Service details
    private String title;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private String serviceType; // ONLINE, IN_PERSON, BOTH
    private String locationAddress;
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

