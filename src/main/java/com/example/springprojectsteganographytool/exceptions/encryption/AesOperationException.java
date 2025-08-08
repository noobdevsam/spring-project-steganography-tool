package com.example.springprojectsteganographytool.exceptions.encryption;

/**
 * Exception thrown when an error occurs during AES (Advanced Encryption Standard) operations.
 * This exception extends the {@link RuntimeException}, making it an unchecked exception.
 */
public class AesOperationException extends RuntimeException {

    /**
     * Constructs a new AesOperationException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public AesOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new AesOperationException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public AesOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}
