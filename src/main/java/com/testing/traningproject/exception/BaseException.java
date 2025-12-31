package com.testing.traningproject.exception;

/**
 * Base exception class for all custom exceptions
 * Extends RuntimeException to avoid checked exception handling
 */
public abstract class BaseException extends RuntimeException {

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

