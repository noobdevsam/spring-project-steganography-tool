package com.example.springprojectsteganographytool.exceptions.common;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class OperationNotAllowedException extends StegoException {

    public OperationNotAllowedException(String message) {
        super(message);
    }

    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.OPERATION_NOT_ALLOWED;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.FORBIDDEN;
    }

}