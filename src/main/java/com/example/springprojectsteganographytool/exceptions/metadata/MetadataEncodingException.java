package com.example.springprojectsteganographytool.exceptions.metadata;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class MetadataEncodingException extends StegoException {

    public MetadataEncodingException(String message) {
        super(message);
    }

    public MetadataEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.METADATA_ENCODING_ERROR;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
