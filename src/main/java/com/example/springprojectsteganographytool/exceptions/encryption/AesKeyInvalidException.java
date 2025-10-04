package com.example.springprojectsteganographytool.exceptions.encryption;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class AesKeyInvalidException extends StegoException {

    public AesKeyInvalidException(String message) {
        super(message);
    }

    public AesKeyInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.ENCRYPTION_KEY_INVALID;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.FORBIDDEN;
    }

}
