package com.example.springprojectsteganographytool.exceptions.common;

/**
 * Exception thrown when an operation is not allowed.
 * This exception extends the {@link RuntimeException}, making it an unchecked exception.
 */
public class OperationNotAllowedException extends RuntimeException {

    /**
     * Constructs a new OperationNotAllowedException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public OperationNotAllowedException(String message) {
        super(message);
    }

    /**
     * Constructs a new OperationNotAllowedException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}