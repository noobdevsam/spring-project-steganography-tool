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

}
