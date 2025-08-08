package com.example.springprojectsteganographytool.exceptions.file;


public class InvalidImageFormatException extends RuntimeException {

    public InvalidImageFormatException(String message) {
        super(message);
    }

    public InvalidImageFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}

