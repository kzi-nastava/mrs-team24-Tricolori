package com.tricolori.backend.exception;

public class UserNotBlockedException extends RuntimeException {
    public UserNotBlockedException() {
        super();
    }
    public UserNotBlockedException(String message) {
        super(message);
    }
}
