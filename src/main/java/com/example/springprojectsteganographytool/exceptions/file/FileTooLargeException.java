package com.example.springprojectsteganographytool.exceptions.file;

/**
 * Exception thrown when a file exceeds the allowed size limit.
 * This exception extends the {@link RuntimeException}, making it an unchecked exception.
 */
public class FileTooLargeException extends RuntimeException {

    /**
     * Constructs a new FileTooLargeException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public FileTooLargeException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileTooLargeException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public FileTooLargeException(String message, Throwable cause) {
        super(message, cause);
    }

}
