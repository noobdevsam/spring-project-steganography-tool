package com.example.springprojectsteganographytool.exceptions.encryption;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class AesOperationException extends StegoException {

    public AesOperationException(String message) {
        super(message);
    }

    public AesOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.ENCRYPTION_PROCESS_ERROR;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
