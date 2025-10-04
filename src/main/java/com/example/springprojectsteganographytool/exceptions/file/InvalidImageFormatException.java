package com.example.springprojectsteganographytool.exceptions.file;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class InvalidImageFormatException extends StegoException {

    public InvalidImageFormatException(String message) {
        super(message);
    }

    public InvalidImageFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.INVALID_IMAGE_FORMAT;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
    }

}
