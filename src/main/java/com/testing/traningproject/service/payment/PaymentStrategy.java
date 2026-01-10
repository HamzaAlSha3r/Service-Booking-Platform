package com.testing.traningproject.service.payment;

import com.testing.traningproject.model.dto.request.PaymentCardRequest;

import java.math.BigDecimal;

/**
 * Strategy Pattern Interface for Payment Processing
 * Supports multiple payment gateways (Stripe, PayPal)
 */
public interface PaymentStrategy {

    /**
     * Process payment
     * @param amount Amount to charge
     * @param cardRequest Payment card details
     * @param description Payment description
     * @return Transaction ID from payment gateway
     */
    String processPayment(BigDecimal amount, PaymentCardRequest cardRequest, String description);

    /**
     * Process refund
     * @param originalTransactionId Original transaction ID
     * @param amount Amount to refund
     * @return Refund transaction ID
     */
    String processRefund(String originalTransactionId, BigDecimal amount);

    /**
     * Process payout to service provider
     * @param amount Amount to payout
     * @param recipientEmail Provider email
     * @param description Payout description
     * @return Payout transaction ID
     */
    String processPayout(BigDecimal amount, String recipientEmail, String description);

    /**
     * Get payment method name
     */
    String getPaymentMethodName();
}

