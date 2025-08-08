package com.example.springprojectsteganographytool.exceptions.data;

/**
 * Exception thrown when an error occurs during storage operations.
 * This exception extends the {@link RuntimeException}, making it an unchecked exception.
 */
public class StorageException extends RuntimeException {

    /**
     * Constructs a new StorageException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * Constructs a new StorageException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

}
