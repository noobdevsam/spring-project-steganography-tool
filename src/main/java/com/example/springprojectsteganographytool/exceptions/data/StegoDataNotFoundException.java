package com.example.springprojectsteganographytool.exceptions.data;

/**
 * Exception thrown when steganographic data is not found.
 * This exception extends the {@link RuntimeException}, making it an unchecked exception.
 */
public class StegoDataNotFoundException extends RuntimeException {

    /**
     * Constructs a new StegoDataNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public StegoDataNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new StegoDataNotFoundException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public StegoDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
