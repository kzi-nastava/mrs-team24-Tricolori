package com.tricolori.backend.core.exceptions;

public class CancelRideExpired extends RuntimeException {
    public CancelRideExpired(String message) {
        super(message);
    }
}
