package com.example.springprojectsteganographytool.exceptions;

import com.example.springprojectsteganographytool.exceptions.common.OperationNotAllowedException;
import com.example.springprojectsteganographytool.exceptions.data.MessageTooLargeException;
import com.example.springprojectsteganographytool.exceptions.data.StegoDataNotFoundException;
import com.example.springprojectsteganographytool.exceptions.data.StorageException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;
import com.example.springprojectsteganographytool.exceptions.encryption.InvalidEncryptionKeyException;
import com.example.springprojectsteganographytool.exceptions.file.FileTooLargeException;
import com.example.springprojectsteganographytool.exceptions.file.FileTypeNotSupportedException;
import com.example.springprojectsteganographytool.exceptions.file.InvalidImageFormatException;
import com.example.springprojectsteganographytool.exceptions.file.StegoImageNotFoundException;
import com.example.springprojectsteganographytool.exceptions.lsb.InvalidLsbDepthException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbDecodingException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbEncodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataDecodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataEncodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;

/**
 * Global exception handler for the application.
 * This class uses Spring's @ControllerAdvice to handle exceptions globally
 * and return appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Builds a standardized response body for exceptions.
     *
     * @param message the error message to include in the response
     * @param status  the HTTP status to set for the response
     * @return a ResponseEntity containing the response body and status
     */
    private ResponseEntity<Object> buildResponse(String message, HttpStatus status) {
        var body = new HashMap<String, Object>();

        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }

    /**
     * Handles FileTypeNotSupportedException and returns a 400 Bad Request response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(FileTypeNotSupportedException.class)
    public ResponseEntity<Object> handleFileTypeNotSupported(FileTypeNotSupportedException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles FileTooLargeException and returns a 413 Payload Too Large response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<Object> handleFileTooLarge(FileTooLargeException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * Handles InvalidEncryptionKeyException and returns a 400 Bad Request response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(InvalidEncryptionKeyException.class)
    public ResponseEntity<Object> handleInvalidEncryptionKey(InvalidEncryptionKeyException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles InvalidLsbDepthException and returns a 400 Bad Request response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(InvalidLsbDepthException.class)
    public ResponseEntity<Object> handleInvalidLsbDepth(InvalidLsbDepthException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles MessageTooLargeException and returns a 413 Payload Too Large response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(MessageTooLargeException.class)
    public ResponseEntity<Object> handleMessageTooLarge(MessageTooLargeException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * Handles MetadataNotFoundException and returns a 404 Not Found response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(MetadataNotFoundException.class)
    public ResponseEntity<Object> handleMetadataNotFound(MetadataNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles StegoImageNotFoundException and returns a 404 Not Found response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(StegoImageNotFoundException.class)
    public ResponseEntity<Object> handleStegoImageNotFound(StegoImageNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles InvalidImageFormatException and returns a 400 Bad Request response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(InvalidImageFormatException.class)
    public ResponseEntity<Object> handleInvalidImageFormat(InvalidImageFormatException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles various processing-related exceptions and returns a 500 Internal Server Error response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler({
            LsbEncodingException.class,
            LsbDecodingException.class,
            AesKeyInvalidException.class,
            AesOperationException.class,
            MetadataEncodingException.class,
            MetadataDecodingException.class
    })
    public ResponseEntity<Object> handleProcessingErrors(RuntimeException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles StegoDataNotFoundException and returns a 404 Not Found response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(StegoDataNotFoundException.class)
    public ResponseEntity<Object> handleStegoDataNotFound(StegoDataNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles StorageException and returns a 500 Internal Server Error response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<Object> handleStorageError(StorageException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles OperationNotAllowedException and returns a 403 Forbidden response.
     *
     * @param ex the exception to handle
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<Object> handleOperationNotAllowed(OperationNotAllowedException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

}
