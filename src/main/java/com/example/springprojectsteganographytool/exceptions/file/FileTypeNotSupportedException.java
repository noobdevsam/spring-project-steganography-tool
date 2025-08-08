package com.example.springprojectsteganographytool.exceptions.file;

/**
 * Exception thrown when an unsupported file type is encountered.
 * This exception extends the {@link RuntimeException}, allowing it to be used
 * for unchecked exceptions.
 */
public class FileTypeNotSupportedException extends RuntimeException {

    /**
     * Constructs a new FileTypeNotSupportedException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public FileTypeNotSupportedException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileTypeNotSupportedException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public FileTypeNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

}
