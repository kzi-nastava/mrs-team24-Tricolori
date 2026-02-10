package com.tricolori.backend.exception;

public class FavoriteRouteNotFoundException extends RuntimeException {
    public FavoriteRouteNotFoundException() {
        super();
    }
    public FavoriteRouteNotFoundException(String message) {
        super(message);
    }
}
