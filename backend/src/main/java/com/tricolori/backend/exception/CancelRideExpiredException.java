package com.tricolori.backend.exception;

public class CancelRideExpiredException extends RuntimeException {
    public CancelRideExpiredException(String message) {
        super(message);
    }
}
