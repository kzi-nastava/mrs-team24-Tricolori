package com.tricolori.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tricolori.backend.dto.error.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRideNotFound(RideNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
        NoRouteGeometryException.class,
        NoSuitableDriversException.class,
        NoFreeDriverCloseException.class
    })
    public ResponseEntity<ErrorResponse> handleRideOrderingErrors(RuntimeException ex) {
        log.warn("Ride ordering failed: {} - Message: {}",
            ex.getClass().getSimpleName(),
            ex.getMessage()
        );

        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        return buildResponse("Unexpected server error.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
        return ResponseEntity
            .status(status)
            .body(new ErrorResponse(message, LocalDateTime.now()));
    }

}