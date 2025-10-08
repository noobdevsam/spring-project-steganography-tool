package com.example.springprojectsteganographytool.exceptions.encryption;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an error occurs during AES encryption or decryption operations.
 * This exception extends the {@link StegoException} class and provides
 * specific error code and HTTP status for AES operation errors.
 */
public class AesOperationException extends StegoException {

    /**
     * Constructs a new AesOperationException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public AesOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new AesOperationException with the specified detail message
     * and the cause of the exception.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception
     */
    public AesOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the specific error code associated with this exception.
     *
     * @return {@link StegoErrorCode#ENCRYPTION_PROCESS_ERROR} indicating the error code
     */
    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.ENCRYPTION_PROCESS_ERROR;
    }

    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return {@link HttpStatus#INTERNAL_SERVER_ERROR} indicating the HTTP status
     */
    @Override
    public HttpStatus status() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}