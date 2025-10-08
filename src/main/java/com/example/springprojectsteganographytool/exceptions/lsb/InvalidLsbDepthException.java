package com.example.springprojectsteganographytool.exceptions.lsb;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invalid LSB (Least Significant Bit) depth is encountered.
 * This exception extends the {@link StegoException} class and provides
 * specific error code and HTTP status for invalid LSB depth scenarios.
 */
public class InvalidLsbDepthException extends StegoException {

    /**
     * Constructs a new InvalidLsbDepthException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidLsbDepthException(String message) {
        super(message);
    }

    /**
     * Returns the specific error code associated with this exception.
     *
     * @return {@link StegoErrorCode#INVALID_LSB_DEPTH} indicating the error code
     */
    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.INVALID_LSB_DEPTH;
    }

    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return {@link HttpStatus#BAD_REQUEST} indicating the HTTP status
     */
    @Override
    public HttpStatus status() {
        return HttpStatus.BAD_REQUEST;
    }

}