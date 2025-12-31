package com.testing.traningproject.exception;

/**
 * Exception thrown when user doesn't have permission
 * HTTP Status: 403 Forbidden
 *
 * Example: Customer accessing admin endpoint, Expired subscription
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
