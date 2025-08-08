package com.example.springprojectsteganographytool.exceptions.lsb;

/**
 * Exception thrown when an error occurs during the LSB (Least Significant Bit) encoding process.
 * This exception extends the {@link RuntimeException}, making it an unchecked exception.
 */
public class LsbEncodingException extends RuntimeException {

    /**
     * Constructs a new LsbEncodingException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public LsbEncodingException(String message) {
        super(message);
    }

    /**
     * Constructs a new LsbEncodingException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public LsbEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

}
