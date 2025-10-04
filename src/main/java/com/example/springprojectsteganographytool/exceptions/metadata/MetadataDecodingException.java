package com.example.springprojectsteganographytool.exceptions.metadata;

import com.example.springprojectsteganographytool.exceptions.StegoErrorCode;
import com.example.springprojectsteganographytool.exceptions.StegoException;
import org.springframework.http.HttpStatus;

public class MetadataDecodingException extends StegoException {

    public MetadataDecodingException(String message) {
        super(message);
    }

    public MetadataDecodingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public StegoErrorCode code() {
        return StegoErrorCode.METADATA_DECODING_ERROR;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
