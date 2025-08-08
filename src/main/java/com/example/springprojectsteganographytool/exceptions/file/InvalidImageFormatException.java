package com.example.springprojectsteganographytool.exceptions.file;

/**
 * Exception thrown when an invalid image format is encountered.
 * This exception extends the {@link RuntimeException}, making it an unchecked exception.
 */
public class InvalidImageFormatException extends RuntimeException {

    /**
     * Constructs a new InvalidImageFormatException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidImageFormatException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidImageFormatException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public InvalidImageFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}
