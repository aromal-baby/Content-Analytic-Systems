package com.contentanalytics.content_analytic_system.exception;

// Custom exception for invalid platform operations
public class PlatformOperationException extends RuntimeException{
    public PlatformOperationException(String message) {
        super(message);
    }
}