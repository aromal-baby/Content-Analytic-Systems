package com.contentanalytics.content_analytic_system.exception;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public  ErrorResponse(){
    }
}
