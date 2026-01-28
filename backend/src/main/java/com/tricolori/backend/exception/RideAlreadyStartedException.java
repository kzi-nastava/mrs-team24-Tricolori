package com.tricolori.backend.exception;

public class RideAlreadyStartedException extends RuntimeException {
    public RideAlreadyStartedException() {
        super();
    }
    public RideAlreadyStartedException(String message) {
        super(message);
    }
}