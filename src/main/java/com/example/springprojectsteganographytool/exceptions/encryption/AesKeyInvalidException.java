package com.example.springprojectsteganographytool.exceptions.encryption;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an AES encryption key is invalid.
 * This exception extends the {@link StegoException} class and provides
 * specific error code and HTTP status for the invalid AES key scenario.
 */
public class AesKeyInvalidException extends StegoException {

    /**
     * Constructs a new AesKeyInvalidException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public AesKeyInvalidException(String message) {
        super(message);
    }

    /**
     * Returns the specific error code associated with this exception.
     *
     * @return {@link StegoErrorCode#ENCRYPTION_KEY_INVALID} indicating the error code
     */
    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.ENCRYPTION_KEY_INVALID;
    }

    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return {@link HttpStatus#FORBIDDEN} indicating the HTTP status
     */
    @Override
    public HttpStatus status() {
        return HttpStatus.FORBIDDEN;
    }

}