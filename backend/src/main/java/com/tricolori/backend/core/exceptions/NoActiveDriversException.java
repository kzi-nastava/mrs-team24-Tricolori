package com.tricolori.backend.core.exceptions;

public class NoActiveDriversException extends RuntimeException {
    public NoActiveDriversException() {
        super();
    }
    public NoActiveDriversException(String message) {
        super(message);
    }
}