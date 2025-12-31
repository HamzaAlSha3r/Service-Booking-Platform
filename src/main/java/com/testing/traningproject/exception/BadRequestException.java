package com.testing.traningproject.exception;

/**
 * Exception thrown when request data is invalid
 * HTTP Status: 400 Bad Request
 *
 * Example: Invalid date, Invalid price, Invalid booking time
 */
public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
