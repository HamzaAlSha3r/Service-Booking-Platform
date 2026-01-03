package com.testing.traningproject.model.dto.request;

import jakarta.validation.Valid;
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

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // "Credit Card", "Debit Card"

    @NotNull(message = "Payment card information is required")
    @Valid
    private PaymentCardRequest paymentCard;

    @Builder.Default
    private Boolean autoRenew = true;
}

