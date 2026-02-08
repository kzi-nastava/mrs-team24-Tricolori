package com.tricolori.backend.exception;

public class AllDriversReachedWorkingLimitException extends RuntimeException {
    public AllDriversReachedWorkingLimitException() {
        super();
    }
    public AllDriversReachedWorkingLimitException(String message) {
        super(message);
    }
}
