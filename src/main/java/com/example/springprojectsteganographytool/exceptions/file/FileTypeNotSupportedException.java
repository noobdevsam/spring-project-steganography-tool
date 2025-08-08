package com.example.springprojectsteganographytool.exceptions.file;

public class FileTypeNotSupportedException extends RuntimeException {

    public FileTypeNotSupportedException(String message) {
        super(message);
    }

    public FileTypeNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
