package com.example.springprojectsteganographytool.exceptions.metadata;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class MetadataNotFoundException extends StegoException {

    public MetadataNotFoundException(String message) {
        super(message);
    }

    public MetadataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.METADATA_NOT_FOUND;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }
}