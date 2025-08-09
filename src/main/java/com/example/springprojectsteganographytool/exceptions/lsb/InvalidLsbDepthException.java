package com.example.springprojectsteganographytool.exceptions.lsb;

/**
 * Custom exception class that represents an invalid LSB (Least Significant Bit) depth error.
 * This exception is thrown when the specified LSB depth is invalid or unsupported.
 */
public class InvalidLsbDepthException extends RuntimeException {

    /**
     * Constructs a new InvalidLsbDepthException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidLsbDepthException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidLsbDepthException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public InvalidLsbDepthException(String message, Throwable cause) {
        super(message, cause);
    }

}
