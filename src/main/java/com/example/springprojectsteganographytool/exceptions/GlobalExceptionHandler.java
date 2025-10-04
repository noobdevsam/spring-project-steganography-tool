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
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * This class uses Spring's @ControllerAdvice to handle exceptions globally
 * and return appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StegoException.class)
    public ProblemDetail handleStego(
            StegoException ex,
            HttpServletRequest request
    ) {
        var pd = ProblemDetail.forStatusAndDetail(ex.status(), ex.getMessage());
        var traceId = MDC.get("traceId");

        pd.setType(URI.create(ex.code().typeURI()));
        pd.setTitle(ex.code().name());
        pd.setProperty("code", ex.code().name());
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("path", request.getRequestURI());

        if (traceId != null) {
            pd.setProperty("traceId", traceId);
        }

        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        var summary = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining());
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, summary);
        pd.setType(URI.create("https://api.example.com/errors/VALIDATION_ERROR"));
        pd.setTitle("VALIDATION_ERROR");
        pd.setProperty("code", "VALIDATION_ERROR");
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("path", request.getRequestURI());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        // Log full stack trace
        // logger.error("Unhandled exception", ex);
        var pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred."
        );

        pd.setType(URI.create("https://api.example.com/errors/INTERNAL_SERVER_ERROR"));
        pd.setTitle("INTERNAL_SERVER_ERROR");
        pd.setProperty("code", "INTERNAL_SERVER_ERROR");
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("path", request.getRequestURI());

        return pd;
    }


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
     * Handles AesKeyInvalidException (e.g., wrong password supplied for decode)
     * Returning 403 clarifies it's an authentication/authorization style failure
     * rather than a server error.
     */
    @ExceptionHandler(AesKeyInvalidException.class)
    public ResponseEntity<Object> handleAesKeyInvalid(AesKeyInvalidException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
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
