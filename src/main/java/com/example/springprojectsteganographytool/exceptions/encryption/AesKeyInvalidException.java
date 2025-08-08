package com.example.springprojectsteganographytool.exceptions.encryption;

/**
 * Exception thrown when an invalid AES (Advanced Encryption Standard) key is encountered.
 * This exception extends the {@link RuntimeException}, making it an unchecked exception.
 */
public class AesKeyInvalidException extends RuntimeException {

    /**
     * Constructs a new AesKeyInvalidException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public AesKeyInvalidException(String message) {
        super(message);
    }

    /**
     * Constructs a new AesKeyInvalidException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public AesKeyInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

}
