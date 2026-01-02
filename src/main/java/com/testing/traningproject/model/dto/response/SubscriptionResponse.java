package com.testing.traningproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for current subscription
 * يُستخدم لعرض معلومات اشتراك Provider الحالي
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {

    private Long id;
    private Long providerId;
    private String providerName;
    private String providerEmail;

    // Plan details
    private Integer planId;
    private String planName;
    private BigDecimal planPrice;
    private Integer planDurationDays;

    // Subscription details
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // ACTIVE, EXPIRED, CANCELLED
    private Boolean autoRenew;
    private Long daysRemaining;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

