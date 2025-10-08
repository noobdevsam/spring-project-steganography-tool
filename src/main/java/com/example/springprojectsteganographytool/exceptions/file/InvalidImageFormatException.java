package com.example.springprojectsteganographytool.exceptions.file;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invalid image format is encountered.
 * This exception extends the {@link StegoException} class and provides
 * specific error code and HTTP status for invalid image format scenarios.
 */
public class InvalidImageFormatException extends StegoException {

    /**
     * Constructs a new InvalidImageFormatException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidImageFormatException(String message) {
        super(message);
    }

    /**
     * Returns the specific error code associated with this exception.
     *
     * @return {@link StegoErrorCode#INVALID_IMAGE_FORMAT} indicating the error code
     */
    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.INVALID_IMAGE_FORMAT;
    }

    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return {@link HttpStatus#UNSUPPORTED_MEDIA_TYPE} indicating the HTTP status
     */
    @Override
    public HttpStatus status() {
        return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
    }

}