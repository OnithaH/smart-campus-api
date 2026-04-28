package com.smartcampus.api.exception;

/**
 * Thrown when a DELETE is attempted on a room that still has sensors attached.
 * Mapped to HTTP 409 Conflict by {@link com.smartcampus.api.mapper.RoomNotEmptyExceptionMapper}.
 */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message);
    }
}
