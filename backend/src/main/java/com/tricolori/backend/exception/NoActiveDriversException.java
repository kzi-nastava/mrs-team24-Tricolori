package com.tricolori.backend.exception;

public class NoActiveDriversException extends RuntimeException {
    public NoActiveDriversException() {
        super();
    }
    public NoActiveDriversException(String message) {
        super(message);
    }
}