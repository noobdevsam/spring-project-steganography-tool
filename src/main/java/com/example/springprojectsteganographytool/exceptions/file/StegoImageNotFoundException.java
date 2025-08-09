package com.example.springprojectsteganographytool.exceptions.file;

/**
 * Custom exception class that represents a stego image not found error.
 * This exception is thrown when the requested stego image file cannot be located.
 */
public class StegoImageNotFoundException extends RuntimeException {

    /**
     * Constructs a new StegoImageNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public StegoImageNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new StegoImageNotFoundException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public StegoImageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}