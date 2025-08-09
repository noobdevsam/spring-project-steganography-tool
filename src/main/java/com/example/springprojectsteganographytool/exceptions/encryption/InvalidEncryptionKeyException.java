package com.example.springprojectsteganographytool.exceptions.encryption;

/**
 * Custom exception class that represents an invalid encryption key error.
 * This exception is thrown when an encryption key provided is invalid or
 * does not meet the required criteria.
 */
public class InvalidEncryptionKeyException extends RuntimeException {

    /**
     * Constructs a new InvalidEncryptionKeyException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidEncryptionKeyException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidEncryptionKeyException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public InvalidEncryptionKeyException(String message, Throwable cause) {
        super(message, cause);
    }

}