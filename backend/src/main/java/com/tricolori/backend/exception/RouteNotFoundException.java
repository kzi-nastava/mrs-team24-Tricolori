package com.tricolori.backend.exception;

public class RouteNotFoundException extends RuntimeException {
    public RouteNotFoundException() {
        super();
    }
    public RouteNotFoundException(String message) {
        super(message);
    }
}
