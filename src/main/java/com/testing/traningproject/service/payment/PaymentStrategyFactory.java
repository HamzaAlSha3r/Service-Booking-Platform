package com.testing.traningproject.service.payment;

import com.testing.traningproject.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Factory Pattern for Payment Strategy Selection
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> paymentStrategies;

    /**
     * Get payment strategy based on payment method
     * @param paymentMethod Payment method (stripe, paypal)
     * @return PaymentStrategy implementation
     */
    public PaymentStrategy getStrategy(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            log.info("No payment method specified, defaulting to Stripe");
            return paymentStrategies.get("stripePayment");
        }

        String normalizedMethod = paymentMethod.toLowerCase().trim();

        PaymentStrategy strategy = switch (normalizedMethod) {
            case "stripe", "card", "credit_card" -> paymentStrategies.get("stripePayment");
            case "paypal" -> paymentStrategies.get("paypalPayment");
            default -> throw new BadRequestException("Unsupported payment method: " + paymentMethod +
                    ". Supported methods: stripe, paypal, card");
        };

        if (strategy == null) {
            log.error("Payment strategy not found for method: {}", paymentMethod);
            throw new BadRequestException("Payment method not available: " + paymentMethod);
        }

        log.info("Selected payment strategy: {} for method: {}", strategy.getClass().getSimpleName(), paymentMethod);
        return strategy;
    }
}

