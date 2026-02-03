package com.tricolori.backend.exception;

public class NoSuitableDriversException extends RuntimeException {
    public NoSuitableDriversException() {
        super();
    }
    public NoSuitableDriversException(String message) {
        super(message);
    }
}