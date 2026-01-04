package com.testing.traningproject.service;

import com.testing.traningproject.exception.BadRequestException;
import com.testing.traningproject.model.dto.request.PaymentCardRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Payment Service - Mock Payment Processing
 * Validates payment card information using industry-standard algorithms
 * NOTE: This is a MOCK service for demonstration. In production, use real payment gateway (Stripe/PayPal)
 */
@Service
@Slf4j
public class PaymentService {

    /**
     * Validate and process payment (MOCK)
     * Returns mock transaction ID
     */
    public String processPayment(PaymentCardRequest card, java.math.BigDecimal amount, String description) {
        log.info("Processing payment - Amount: ${}, Description: {}", amount, description);

        // 1. Validate card number using Luhn Algorithm
        if (!isValidCardNumber(card.getCardNumber())) {
            throw new BadRequestException("Invalid card number. Please check and try again.");
        }

        // 2. Validate expiry date
        if (!isValidExpiryDate(card.getExpiryMonth(), card.getExpiryYear())) {
            throw new BadRequestException("Card has expired or invalid expiry date.");
        }

        // 3. Validate CVV format (already validated by @Pattern, but double-check)
        if (!card.getCvv().matches("^[0-9]{3,4}$")) {
            throw new BadRequestException("Invalid CVV format.");
        }

        // 4. Check for test/blocked cards
        if (isBlockedCard(card.getCardNumber())) {
            throw new BadRequestException("This card has been declined. Please use a different card.");
        }

        // 5. Simulate payment processing delay
        try {
            Thread.sleep(500); // Simulate network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 6. Generate mock transaction ID
        String transactionId = "TXN_" + System.currentTimeMillis() + "_" + card.getCardNumber().substring(12);

        log.info("Payment processed successfully - Transaction ID: {}", transactionId);
        log.info("Card: {}**********{}", card.getCardNumber().substring(0, 4), card.getCardNumber().substring(12));

        return transactionId;
    }

    /**
     * Validate card number using Luhn Algorithm (Mod 10)
     * This is the industry-standard algorithm for credit card validation
     */
    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("^[0-9]{16}$")) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        // Start from the rightmost digit
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        boolean isValid = (sum % 10 == 0);

        if (!isValid) {
            log.warn("Card number failed Luhn check: {}**********{}",
                    cardNumber.substring(0, 4), cardNumber.substring(12));
        }

        return isValid;
    }

    /**
     * Validate expiry date
     */
    private boolean isValidExpiryDate(Integer month, Integer year) {
        if (month == null || year == null) {
            return false;
        }

        // Check month range
        if (month < 1 || month > 12) {
            return false;
        }

        // Get current date
        LocalDate now = LocalDate.now();
        YearMonth cardExpiry = YearMonth.of(year, month);
        YearMonth currentMonth = YearMonth.from(now);

        // Card must not be expired
        return !cardExpiry.isBefore(currentMonth);
    }

    /**
     * Check if card is in blocklist (for testing purposes)
     * In production, this would check against fraud detection services
     */
    private boolean isBlockedCard(String cardNumber) {
        // Block test cards that always fail (for testing error scenarios)
        String[] blockedCards = {
            "0000000000000000",  // All zeros
            "1111111111111111",  // All ones
            "9999999999999999"   // All nines
        };

        for (String blocked : blockedCards) {
            if (cardNumber.equals(blocked)) {
                log.warn("Blocked card detected: {}**********{}",
                        cardNumber.substring(0, 4), cardNumber.substring(12));
                return true;
            }
        }

        return false;
    }

    /**
     * Process refund (MOCK)
     */
    public String processRefund(String originalTransactionId, java.math.BigDecimal amount) {
        // Generate mock refund transaction ID ( will update when make connection with orignal gate pay as stripe)
        String refundId = "REFUND_" + System.currentTimeMillis();

        log.info("Refund processed successfully - Refund ID: {}", refundId);

        return refundId;
    }

    /**
     * Mask card number for security (show only first 4 and last 4 digits)
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "********" + cardNumber.substring(12);
    }
}

