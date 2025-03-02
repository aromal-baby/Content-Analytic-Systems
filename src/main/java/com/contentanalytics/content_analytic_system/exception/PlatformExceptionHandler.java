package com.contentanalytics.content_analytic_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
//Global exception handler for platform-related errors
public class PlatformExceptionHandler {
    // Method to handle platform-specific operation exceptions
    @ExceptionHandler(PlatformOperationException.class)
    public ResponseEntity<ErrorResponse> handlePlatformOperationException(PlatformOperationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
