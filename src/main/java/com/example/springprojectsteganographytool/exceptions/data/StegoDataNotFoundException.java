package com.example.springprojectsteganographytool.exceptions.data;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class StegoDataNotFoundException extends StegoException {

    public StegoDataNotFoundException(String message) {
        super(message);
    }

    public StegoDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.STEGO_DATA_NOT_FOUND;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}
