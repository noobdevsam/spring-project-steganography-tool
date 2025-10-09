package com.example.springprojectsteganographytool.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * This class uses Spring's @ControllerAdvice to handle exceptions globally
 * and return appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions of type StegoException.
     *
     * @param ex      the StegoException instance
     * @param request the HttpServletRequest object containing request details
     * @return a ProblemDetail object containing error details
     */
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

    /**
     * Handles exceptions of type MethodArgumentNotValidException.
     *
     * @param ex      the MethodArgumentNotValidException instance
     * @param request the HttpServletRequest object containing request details
     * @return a ProblemDetail object containing validation error details
     */
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
        var traceId = MDC.get("traceId");

        pd.setType(URI.create("https://api.example.com/errors/VALIDATION_ERROR"));
        pd.setTitle(StegoErrorCode.VALIDATION_ERROR.name());
        pd.setProperty("code", StegoErrorCode.VALIDATION_ERROR.name());
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("path", request.getRequestURI());

        if (traceId != null) {
            pd.setProperty("traceId", traceId);
        }

        return pd;
    }

    /**
     * Handles exceptions of type IllegalArgumentException.
     *
     * @param ex      the IllegalArgumentException instance
     * @param request the HttpServletRequest object containing request details
     * @return a ProblemDetail object containing error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        var traceId = MDC.get("traceId");

        pd.setType(URI.create("https://api.example.com/errors/VALIDATION_ERROR"));
        pd.setTitle("VALIDATION_ERROR");
        pd.setProperty("code", "VALIDATION_ERROR");
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("path", request.getRequestURI());

        if (traceId != null) {
            pd.setProperty("traceId", traceId);
        }

        return pd;
    }

    /**
     * Handles exceptions of type SecurityException.
     *
     * @param ex      the SecurityException instance
     * @param request the HttpServletRequest object containing request details
     * @return a ProblemDetail object containing security error details
     */
    @ExceptionHandler(SecurityException.class)
    public ProblemDetail handleSecurity(
            SecurityException ex,
            HttpServletRequest request
    ) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        var traceId = MDC.get("traceId");

        pd.setType(URI.create("https://api.example.com/errors/STORAGE_SECURITY_ERROR"));
        pd.setTitle("STORAGE_SECURITY_ERROR");
        pd.setProperty("code", "STORAGE_SECURITY_ERROR");
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("path", request.getRequestURI());

        if (traceId != null) {
            pd.setProperty("traceId", traceId);
        }

        return pd;
    }

    /**
     * Handles generic exceptions of type Exception.
     *
     * @param ex      the Exception instance
     * @param request the HttpServletRequest object containing request details
     * @return a ProblemDetail object containing generic error details
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        // Log full stack trace
        // logger.error("Unhandled exception", ex);
        var pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getLocalizedMessage()
        );
        var traceId = MDC.get("traceId");

        pd.setType(URI.create("https://api.example.com/errors/INTERNAL_SERVER_ERROR"));
        pd.setTitle("INTERNAL_SERVER_ERROR");
        pd.setProperty("code", "INTERNAL_SERVER_ERROR");
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("path", request.getRequestURI());

        if (traceId != null) {
            pd.setProperty("traceId", traceId);
        }

        return pd;
    }

}