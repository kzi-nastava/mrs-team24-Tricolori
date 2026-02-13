package com.tricolori.backend.exception;

public class InvalidReportDateException extends RuntimeException {
    public InvalidReportDateException() {
        super();
    }
    public InvalidReportDateException(String message) {
        super(message);
    }
}
