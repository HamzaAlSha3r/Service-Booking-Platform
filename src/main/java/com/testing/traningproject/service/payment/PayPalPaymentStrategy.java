package com.testing.traningproject.service.payment;

import com.testing.traningproject.model.dto.request.PaymentCardRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * PayPal Payment Strategy Implementation (Simulated)
 */
@Slf4j
@Component("paypalPayment")
public class PayPalPaymentStrategy implements PaymentStrategy {

    @Override
    public String processPayment(BigDecimal amount, PaymentCardRequest cardRequest, String description) {
        log.info("Processing PayPal payment - Amount: ${}, Card Holder: {}", amount, cardRequest.getCardHolderName());

        // Simulate PayPal API call
        String transactionId = "PAYPAL_" + System.currentTimeMillis() + "_" +
                              cardRequest.getCardNumber().substring(cardRequest.getCardNumber().length() - 4);

        log.info("PayPal payment successful - Transaction ID: {}", transactionId);
        return transactionId;
    }

    @Override
    public String processRefund(String originalTransactionId, BigDecimal amount) {
        log.info("Processing PayPal refund - Original TXN: {}, Amount: ${}", originalTransactionId, amount);

        // Simulate PayPal refund API call
        String refundId = "PAYPAL_REFUND_" + System.currentTimeMillis();

        log.info("PayPal refund successful - Refund ID: {}", refundId);
        return refundId;
    }

    @Override
    public String processPayout(BigDecimal amount, String recipientEmail, String description) {
        log.info("Processing PayPal payout - Amount: ${}, Recipient: {}", amount, recipientEmail);

        // Simulate PayPal payout API call
        String payoutId = "PAYPAL_PAYOUT_" + System.currentTimeMillis();

        log.info("PayPal payout successful - Payout ID: {}", payoutId);
        return payoutId;
    }

    @Override
    public String getPaymentMethodName() {
        return "paypal";
    }
}

