package com.example.springprojectsteganographytool.exceptions.data;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the requested steganographic data is not found.
 * This exception is a specific type of {@link StegoException} and provides
 * additional context such as an error code and HTTP status.
 */
public class StegoDataNotFoundException extends StegoException {

    /**
     * Constructs a new StegoDataNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public StegoDataNotFoundException(String message) {
        super(message);
    }

    /**
     * Returns the specific error code associated with this exception.
     *
     * @return the error code {@link StegoErrorCode#STEGO_DATA_NOT_FOUND}
     */
    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.STEGO_DATA_NOT_FOUND;
    }

    /**
     * Returns the HTTP status code associated with this exception.
     *
     * @return the HTTP status {@link HttpStatus#NOT_FOUND}
     */
    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}