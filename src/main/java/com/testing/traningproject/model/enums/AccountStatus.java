package com.testing.traningproject.model.enums;

/**
 * Enum for User account status
 * Maps to: CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'PENDING_APPROVAL', 'REJECTED'))
 */
public enum AccountStatus {
    ACTIVE,
    SUSPENDED,
    PENDING_APPROVAL,
    REJECTED
}

