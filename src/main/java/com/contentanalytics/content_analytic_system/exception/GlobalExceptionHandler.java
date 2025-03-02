package com.contentanalytics.content_analytic_system.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Global exception handler
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handle content not found
    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleContentNotFound(ContentNotFoundException ex) {
        // Error with stack trace
        log.error("Content Not Found error:", ex);

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setMessage(ex.getMessage());
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
    // Handles PlatformOperationException
    @ExceptionHandler(PlatformOperationException.class)
    public ResponseEntity<ErrorResponse> handlePlatformOperationException(PlatformOperationException ex) {
        // Stack trace
        log.error("Platform Operation error:", ex);

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage(ex.getMessage());
        error.setTimestamp(LocalDateTime.now());


        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    */

    // Handles all other unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) throws Exception {

        if (ex instanceof PlatformOperationException) {
            log.error("Platform Operation error:", ex);
            throw ex;
        }

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage(ex.getMessage());
        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error:", ex);

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldNAme = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldNAme, errorMessage);
        });

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage("Validation failed");

        error.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
