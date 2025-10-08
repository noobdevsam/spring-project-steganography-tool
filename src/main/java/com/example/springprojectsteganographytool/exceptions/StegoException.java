package com.example.springprojectsteganographytool.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Abstract base class for all custom exceptions in the steganography tool application.
 * This class extends {@link RuntimeException} and provides a structure for defining
 * specific error codes and HTTP statuses for different types of exceptions.
 */
public abstract class StegoException extends RuntimeException {

    /**
     * Constructs a new StegoException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    protected StegoException(String message) {
        super(message);
    }

    /**
     * Constructs a new StegoException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    protected StegoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Abstract method to retrieve the specific error code associated with the exception.
     * Subclasses must implement this method to provide a {@link StegoErrorCode}.
     *
     * @return the {@link StegoErrorCode} representing the specific error
     */
    public abstract StegoErrorCode code();

    /**
     * Abstract method to retrieve the HTTP status to be used in the response
     * when this exception is thrown. Subclasses must implement this method to
     * provide an appropriate {@link HttpStatus}.
     *
     * @return the {@link HttpStatus} representing the HTTP status for the exception
     */
    public abstract HttpStatus status();
}