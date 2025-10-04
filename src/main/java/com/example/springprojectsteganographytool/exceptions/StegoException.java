package com.example.springprojectsteganographytool.exceptions;

import org.springframework.http.HttpStatus;

public abstract class StegoException extends RuntimeException {
    protected StegoException(String message) {
        super(message);
    }

    protected StegoException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract StegoErrorCode code();

    public abstract HttpStatus status();
}
