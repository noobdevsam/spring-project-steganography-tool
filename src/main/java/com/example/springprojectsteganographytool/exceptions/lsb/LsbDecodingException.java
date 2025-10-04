package com.example.springprojectsteganographytool.exceptions.lsb;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class LsbDecodingException extends StegoException {

    public LsbDecodingException(String message) {
        super(message);
    }

    public LsbDecodingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.DECODE_FAILURE;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
