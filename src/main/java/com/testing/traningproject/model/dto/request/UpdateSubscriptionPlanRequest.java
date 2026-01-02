package com.testing.traningproject.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating subscription plan
 * يُستخدم عند تحديث خطة اشتراك موجودة
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSubscriptionPlanRequest {


    private String name;

    private String description;

    @DecimalMin(value = "0.00", message = "Price must be 0 or greater")
    private BigDecimal price;

    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;

    private Boolean isActive;
}

