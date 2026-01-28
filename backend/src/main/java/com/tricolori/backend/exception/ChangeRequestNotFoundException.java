package com.tricolori.backend.exception;

public class ChangeRequestNotFoundException extends RuntimeException {
    public ChangeRequestNotFoundException(String message) {
        super(message);
    }
    public ChangeRequestNotFoundException() {
        super();
    }
}
