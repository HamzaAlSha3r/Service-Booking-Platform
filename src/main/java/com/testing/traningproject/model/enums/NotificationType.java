package com.testing.traningproject.model.enums;

/**
 * Enum for Notification type
 * Maps to: CHECK (notification_type IN (...))
 */
public enum NotificationType {
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    NEW_BOOKING_RECEIVED,
    BOOKING_REMINDER,
    REVIEW_RECEIVED,
    REFUND_APPROVED,
    REFUND_REJECTED,
    SUBSCRIPTION_EXPIRING,
    SUBSCRIPTION_EXPIRED,
    ACCOUNT_APPROVED,
    ACCOUNT_REJECTED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED
}

