package com.tricolori.backend.core.exceptions;

public class NoRouteGeometryException extends RuntimeException {
    public NoRouteGeometryException() {
        super();
    }
    public NoRouteGeometryException(String message) {
        super(message);
    }
}