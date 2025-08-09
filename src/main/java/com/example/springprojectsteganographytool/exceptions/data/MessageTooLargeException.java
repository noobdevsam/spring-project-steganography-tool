package com.example.springprojectsteganographytool.exceptions.data;

/**
 * Custom exception class that represents a message size error.
 * This exception is thrown when a message is too large to be processed
 * or embedded within the available data capacity.
 */
public class MessageTooLargeException extends RuntimeException {

    /**
     * Constructs a new MessageTooLargeException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public MessageTooLargeException(String message) {
        super(message);
    }

    /**
     * Constructs a new MessageTooLargeException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public MessageTooLargeException(String message, Throwable cause) {
        super(message, cause);
    }
}