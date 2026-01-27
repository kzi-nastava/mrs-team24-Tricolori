package com.tricolori.backend.core.exceptions;

public class AllDriversReachedWorkingLimitException extends RuntimeException {
    public AllDriversReachedWorkingLimitException() {
        super();
    }
    public AllDriversReachedWorkingLimitException(String message) {
        super(message);
    }
}
