package com.example.springprojectsteganographytool.exceptions.data;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a message exceeds the allowable size for processing.
 * This exception is a specific type of {@link StegoException} and provides
 * additional context such as an error code and HTTP status.
 */
public class MessageTooLargeException extends StegoException {

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

    /**
     * Returns the specific error code associated with this exception.
     *
     * @return the error code {@link StegoErrorCode#PAYLOAD_TOO_LARGE}
     */
    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.PAYLOAD_TOO_LARGE;
    }

    /**
     * Returns the HTTP status code associated with this exception.
     *
     * @return the HTTP status {@link HttpStatus#PAYLOAD_TOO_LARGE}
     */
    @Override
    public HttpStatus status() {
        return HttpStatus.PAYLOAD_TOO_LARGE;
    }

}