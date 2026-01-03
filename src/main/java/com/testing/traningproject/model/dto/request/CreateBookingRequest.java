package com.testing.traningproject.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a booking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Time slot ID is required")
    private Long slotId;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // e.g., "Credit Card", "Debit Card"

    @NotNull(message = "Payment card information is required")
    @Valid
    private PaymentCardRequest paymentCard;
}
