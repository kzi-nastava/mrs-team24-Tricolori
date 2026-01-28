package com.tricolori.backend.exception;

public class DriverHasPendingProfileRequestException  extends RuntimeException {
    public DriverHasPendingProfileRequestException(String message) {
        super(message);
    }
    public DriverHasPendingProfileRequestException() {
        super();
    }
}

