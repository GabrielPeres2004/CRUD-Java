package com.gabriel.crudjava.controller.exceptions;

import java.time.LocalDateTime;

public class StandardError {

    private Integer status;
    private String message;
    private LocalDateTime timestamp;

    public StandardError(Integer status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
