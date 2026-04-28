package com.smartcampus.api.exception;

/**
 * Thrown when a resource references another resource that does not exist
 * (e.g., creating a sensor with a non-existent roomId).
 * Mapped to HTTP 422 Unprocessable Entity by
 * {@link com.smartcampus.api.mapper.LinkedResourceNotFoundExceptionMapper}.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
