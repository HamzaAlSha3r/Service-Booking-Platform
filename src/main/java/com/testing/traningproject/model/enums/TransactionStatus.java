package com.testing.traningproject.model.enums;

/**
 * Enum for Transaction status
 * Maps to: CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'))
 */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}

