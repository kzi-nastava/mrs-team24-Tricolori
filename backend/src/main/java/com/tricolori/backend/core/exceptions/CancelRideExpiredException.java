package com.tricolori.backend.core.exceptions;

public class CancelRideExpiredException extends RuntimeException {
    public CancelRideExpiredException(String message) {
        super(message);
    }
}
