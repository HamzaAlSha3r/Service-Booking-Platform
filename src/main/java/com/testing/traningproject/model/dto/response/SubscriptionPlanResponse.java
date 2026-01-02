package com.testing.traningproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for SubscriptionPlan
 * يُستخدم لعرض معلومات خطة الاشتراك للمستخدمين
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanResponse {

    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private Boolean isActive;
}

