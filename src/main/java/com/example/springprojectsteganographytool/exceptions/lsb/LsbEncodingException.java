package com.example.springprojectsteganographytool.exceptions.lsb;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class LsbEncodingException extends StegoException {

    public LsbEncodingException(String message) {
        super(message);
    }

    public LsbEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.ENCODE_FAILURE;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
