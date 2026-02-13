package com.tricolori.backend.exception;

public class UserAlreadyBlockedException extends RuntimeException {
    public UserAlreadyBlockedException() {
        super();
    }
    public UserAlreadyBlockedException(String message) {
        super(message);
    }
}
