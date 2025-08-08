package com.example.springprojectsteganographytool.exceptions;

import com.example.springprojectsteganographytool.exceptions.common.OperationNotAllowedException;
import com.example.springprojectsteganographytool.exceptions.data.StegoDataNotFoundException;
import com.example.springprojectsteganographytool.exceptions.data.StorageException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;
import com.example.springprojectsteganographytool.exceptions.file.FileTooLargeException;
import com.example.springprojectsteganographytool.exceptions.file.FileTypeNotSupportedException;
import com.example.springprojectsteganographytool.exceptions.file.InvalidImageFormatException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbDecodingException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbEncodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataDecodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataEncodingException;
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

    @ExceptionHandler(StegoDataNotFoundException.class)
    public ResponseEntity<Object> handleStegoDataNotFound(StegoDataNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<Object> handleStorageError(StorageException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<Object> handleOperationNotAllowed(OperationNotAllowedException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

}
