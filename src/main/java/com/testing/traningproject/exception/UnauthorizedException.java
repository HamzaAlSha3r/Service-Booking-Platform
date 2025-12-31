package com.testing.traningproject.exception;

/**
 * Exception thrown when user is not authenticated
 * HTTP Status: 401 Unauthorized
 *
 * Example: Invalid credentials, Expired token, Missing token
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
