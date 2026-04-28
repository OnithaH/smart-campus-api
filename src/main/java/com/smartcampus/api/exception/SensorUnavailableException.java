package com.smartcampus.api.exception;

/**
 * Thrown when a reading is posted to a sensor whose status is MAINTENANCE.
 * Mapped to HTTP 403 Forbidden by
 * {@link com.smartcampus.api.mapper.SensorUnavailableExceptionMapper}.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
