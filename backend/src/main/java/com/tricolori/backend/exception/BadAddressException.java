package com.tricolori.backend.exception;

public class BadAddressException extends RuntimeException {
    public BadAddressException(String message) {
        super(message);
    }
    public BadAddressException() {
        super();
    }
}
