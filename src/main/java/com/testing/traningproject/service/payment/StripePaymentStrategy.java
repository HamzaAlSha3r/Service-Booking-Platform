package com.testing.traningproject.service.payment;

import com.testing.traningproject.model.dto.request.PaymentCardRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Stripe Payment Strategy Implementation (Simulated)
 */
@Slf4j
@Component("stripePayment")
public class StripePaymentStrategy implements PaymentStrategy {

    @Override
    public String processPayment(BigDecimal amount, PaymentCardRequest cardRequest, String description) {
        log.info("Processing Stripe payment - Amount: ${}, Card: {}****{}",
                amount,
                cardRequest.getCardNumber().substring(0, 4),
                cardRequest.getCardNumber().substring(cardRequest.getCardNumber().length() - 4));

        // Simulate Stripe API call
        String transactionId = "STRIPE_" + System.currentTimeMillis() + "_" +
                              cardRequest.getCardNumber().substring(cardRequest.getCardNumber().length() - 4);

        log.info("Stripe payment successful - Transaction ID: {}", transactionId);
        return transactionId;
    }

    @Override
    public String processRefund(String originalTransactionId, BigDecimal amount) {
        log.info("Processing Stripe refund - Original TXN: {}, Amount: ${}", originalTransactionId, amount);

        // Simulate Stripe refund API call
        String refundId = "STRIPE_REFUND_" + System.currentTimeMillis();

        log.info("Stripe refund successful - Refund ID: {}", refundId);
        return refundId;
    }

    @Override
    public String processPayout(BigDecimal amount, String recipientEmail, String description) {
        log.info("Processing Stripe payout - Amount: ${}, Recipient: {}", amount, recipientEmail);

        // Simulate Stripe payout API call
        String payoutId = "STRIPE_PAYOUT_" + System.currentTimeMillis();

        log.info("Stripe payout successful - Payout ID: {}", payoutId);
        return payoutId;
    }

    @Override
    public String getPaymentMethodName() {
        return "stripe";
    }
}

