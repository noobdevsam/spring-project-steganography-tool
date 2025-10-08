package com.example.springprojectsteganographytool.exceptions.data;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is an issue related to storage operations.
 * This exception is a specific type of {@link StegoException} and provides
 * additional context such as an error code and HTTP status.
 */
public class StorageException extends StegoException {

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

    /**
     * Returns the specific error code associated with this exception.
     *
     * @return the error code {@link StegoErrorCode#STORAGE_ERROR}
     */
    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.STORAGE_ERROR;
    }

    /**
     * Returns the HTTP status code associated with this exception.
     *
     * @return the HTTP status {@link HttpStatus#INTERNAL_SERVER_ERROR}
     */
    @Override
    public HttpStatus status() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}