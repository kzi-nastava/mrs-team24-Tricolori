package com.tricolori.backend.exception;

public class FavoriteRouteAlreadyExistsException extends RuntimeException {
    public FavoriteRouteAlreadyExistsException() {
        super();
    }
    public FavoriteRouteAlreadyExistsException(String message) {
        super(message);
    }
}
