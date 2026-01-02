package com.testing.traningproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for Provider Availability
 * يُستخدم لعرض جدول توفر مقدم الخدمة
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderAvailabilityResponse {

    private Long id;
    private Long providerId;
    private String providerName;
    private String dayOfWeek; // MONDAY, TUESDAY, etc.
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

