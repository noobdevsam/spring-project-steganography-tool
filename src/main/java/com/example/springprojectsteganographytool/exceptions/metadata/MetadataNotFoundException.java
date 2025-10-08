package com.example.springprojectsteganographytool.exceptions.metadata;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

/**
 * Exception class representing a metadata not found error.
 * This exception extends the {@link StegoException} class and provides
 * specific error code and HTTP status for cases where metadata is not found.
 */
public class MetadataNotFoundException extends StegoException {

    /**
     * Constructs a new MetadataNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public MetadataNotFoundException(String message) {
        super(message);
    }

    /**
     * Returns the specific error code associated with this exception.
     *
     * @return the {@link StegoErrorCode} representing a metadata not found error
     */
    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.METADATA_NOT_FOUND;
    }

    /**
     * Returns the HTTP status to be used in the response when this exception is thrown.
     *
     * @return the {@link HttpStatus#NOT_FOUND} indicating a client-side error
     */
    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}