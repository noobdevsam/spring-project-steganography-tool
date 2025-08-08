package com.example.springprojectsteganographytool.exceptions;

import com.example.springprojectsteganographytool.exceptions.file.FileTooLargeException;
import com.example.springprojectsteganographytool.exceptions.file.FileTypeNotSupportedException;
import com.example.springprojectsteganographytool.exceptions.file.InvalidImageFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> buildResponse(String message, HttpStatus status) {
        var body = new HashMap<String, Object>();

        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(FileTypeNotSupportedException.class)
    public ResponseEntity<Object> handleFileTypeNotSupported(FileTypeNotSupportedException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<Object> handleFileTooLarge(FileTooLargeException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(InvalidImageFormatException.class)
    public ResponseEntity<Object> handleInvalidImageFormat(InvalidImageFormatException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
