package com.example.springprojectsteganographytool.exceptions.lsb;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an error occurs during the decoding process
 * of LSB (Least Significant Bit) steganography.
 * This exception extends the {@link StegoException} class and provides
 * specific error code and HTTP status for LSB decoding failures.
 */
public class LsbDecodingException extends StegoException {

    /**
     * Constructs a new LsbDecodingException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public LsbDecodingException(String message) {
        super(message);
    }

    /**
     * Constructs a new LsbDecodingException with the specified detail message
     * and the cause of the exception.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the cause of the exception
     */
    public LsbDecodingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns the specific error code associated with this exception.
     *
     * @return {@link StegoErrorCode#DECODE_FAILURE} indicating the error code
     */
    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.DECODE_FAILURE;
    }

    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return {@link HttpStatus#UNPROCESSABLE_ENTITY} indicating the HTTP status
     */
    @Override
    public HttpStatus status() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }

}