package com.example.springprojectsteganographytool.exceptions.metadata;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

/**
 * Exception class representing an error that occurs during the decoding of metadata.
 * This exception extends the {@link StegoException} class and provides specific
 * error code and HTTP status for metadata decoding errors.
 */
public class MetadataDecodingException extends StegoException {

    /**
     * Constructs a new MetadataDecodingException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public MetadataDecodingException(String message) {
        super(message);
    }

    /**
     * Constructs a new MetadataDecodingException with the specified detail message
     * and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception (a throwable that caused this exception)
     */
    public MetadataDecodingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the specific error code associated with this exception.
     *
     * @return the {@link StegoErrorCode} representing a metadata decoding error
     */
    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.METADATA_DECODING_ERROR;
    }

    /**
     * Returns the HTTP status to be used in the response when this exception is thrown.
     *
     * @return the {@link HttpStatus#INTERNAL_SERVER_ERROR} indicating a server-side error
     */
    @Override
    public HttpStatus status() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}