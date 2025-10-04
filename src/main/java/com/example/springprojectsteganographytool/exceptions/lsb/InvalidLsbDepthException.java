package com.example.springprojectsteganographytool.exceptions.lsb;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class InvalidLsbDepthException extends StegoException {

    public InvalidLsbDepthException(String message) {
        super(message);
    }

    public InvalidLsbDepthException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.INVALID_LSB_DEPTH;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.BAD_REQUEST;
    }

}
