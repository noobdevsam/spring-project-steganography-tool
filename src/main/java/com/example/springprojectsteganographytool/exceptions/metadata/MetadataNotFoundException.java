package com.example.springprojectsteganographytool.exceptions.metadata;

/**
 * Custom exception class that represents a metadata not found error.
 * This exception is thrown when the requested metadata cannot be located.
 */
public class MetadataNotFoundException extends RuntimeException {

    /**
     * Constructs a new MetadataNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public MetadataNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new MetadataNotFoundException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public MetadataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}