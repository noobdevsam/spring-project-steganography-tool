package com.example.springprojectsteganographytool.exceptions.metadata;

/**
 * Exception thrown when an error occurs during the encoding of metadata.
 * This exception extends the {@link RuntimeException}, making it an unchecked exception.
 */
public class MetadataEncodingException extends RuntimeException {

    /**
     * Constructs a new MetadataEncodingException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public MetadataEncodingException(String message) {
        super(message);
    }

    /**
     * Constructs a new MetadataEncodingException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public MetadataEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

}
