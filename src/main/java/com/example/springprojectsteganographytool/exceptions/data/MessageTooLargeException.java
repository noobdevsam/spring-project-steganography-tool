package com.example.springprojectsteganographytool.exceptions.data;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class MessageTooLargeException extends StegoException {

    public MessageTooLargeException(String message) {
        super(message);
    }

    public MessageTooLargeException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.PAYLOAD_TOO_LARGE;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.PAYLOAD_TOO_LARGE;
    }

}