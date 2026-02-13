package com.tricolori.backend.exception;

public class BadReportIndividualEmailException extends RuntimeException {
    public BadReportIndividualEmailException() {
        super();
    }
    public BadReportIndividualEmailException(String message) {
        super(message);
    }
}
