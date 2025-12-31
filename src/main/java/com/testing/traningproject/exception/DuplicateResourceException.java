package com.testing.traningproject.exception;

/**
 * Exception thrown when trying to create a duplicate resource
 * HTTP Status: 409 Conflict
 *
 * Example: Email already exists, Duplicate booking for same slot
 */
public class DuplicateResourceException extends BaseException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String field, Object value) {
        super(String.format("%s with %s '%s' already exists", resourceName, field, value));
    }
}
