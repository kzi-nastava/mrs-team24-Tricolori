package com.tricolori.backend.exception;

public class ForeignRideException extends RuntimeException {
    public ForeignRideException() {
        super();
    }
    public ForeignRideException(String message) {
        super(message);
    }
}