package com.testing.traningproject.exception;

/**
 * Exception thrown when a requested resource is not found
 * HTTP Status: 404 Not Found
 *
 * Example: User not found, Service not found, Booking not found
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s with id %d not found", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(String.format("%s with %s '%s' not found", resourceName, field, value));
    }
}
