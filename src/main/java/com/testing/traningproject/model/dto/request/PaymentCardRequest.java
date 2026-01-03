package com.testing.traningproject.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Payment Card Request DTO
 * Used for collecting payment card information (Mock - for demonstration)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCardRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be 16 digits")
    private String cardNumber;

    @NotBlank(message = "Card holder name is required")
    @Size(min = 3, max = 100, message = "Card holder name must be between 3 and 100 characters")
    private String cardHolderName;

    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer expiryMonth;

    @NotNull(message = "Expiry year is required")
    @Min(value = 2026, message = "Card has expired")
    @Max(value = 2036, message = "Invalid expiry year")
    private Integer expiryYear;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;

    @NotBlank(message = "Billing address is required")
    @Size(min = 10, max = 500, message = "Billing address must be between 10 and 500 characters")
    private String billingAddress;
}

