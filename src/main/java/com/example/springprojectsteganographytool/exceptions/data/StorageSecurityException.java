package com.example.springprojectsteganographytool.exceptions.data;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is a security-related issue during storage operations.
 * This exception is a specific type of {@link StegoException} and provides
 * additional context such as an error code and HTTP status.
 */
public class StorageSecurityException extends StegoException {

    /**
     * Constructs a new StorageSecurityException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public StorageSecurityException(String message) {
        super(message);
    }

    /**
     * Returns the specific error code associated with this exception.
     *
     * @return the error code {@link StegoErrorCode#STORAGE_SECURITY_ERROR}
     */
    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.STORAGE_SECURITY_ERROR;
    }

    /**
     * Returns the HTTP status code associated with this exception.
     *
     * @return the HTTP status {@link HttpStatus#FORBIDDEN}
     */
    @Override
    public HttpStatus status() {
        return HttpStatus.FORBIDDEN;
    }
}