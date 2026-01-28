package com.tricolori.backend.exception;

public class NoRouteGeometryException extends RuntimeException {
    public NoRouteGeometryException() {
        super();
    }
    public NoRouteGeometryException(String message) {
        super(message);
    }
}