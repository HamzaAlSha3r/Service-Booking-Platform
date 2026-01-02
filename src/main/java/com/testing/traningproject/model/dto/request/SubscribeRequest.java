package com.testing.traningproject.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for subscribing to a plan
 * يُستخدم عند اشتراك Provider في خطة معينة
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscribeRequest {

    @NotNull(message = "Plan ID is required")
    private Integer planId;

    private String paymentMethod; // "STRIPE", "PAYPAL", etc.

    @Builder.Default
    private Boolean autoRenew = true;
}

