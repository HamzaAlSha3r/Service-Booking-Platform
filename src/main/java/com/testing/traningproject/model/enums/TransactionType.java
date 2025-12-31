package com.testing.traningproject.model.enums;

/**
 * Enum for Transaction type
 * Maps to: CHECK (transaction_type IN ('BOOKING_PAYMENT', 'SUBSCRIPTION_PAYMENT', 'REFUND', 'PAYOUT'))
 */
public enum TransactionType {
    BOOKING_PAYMENT,
    SUBSCRIPTION_PAYMENT,
    REFUND,
    PAYOUT
}

