package com.tricolori.backend.exception;

public class NoFreeDriverCloseException extends RuntimeException {
    public NoFreeDriverCloseException() {
        super();
    }
    public NoFreeDriverCloseException(String message) {
        super(message);
    }
}
