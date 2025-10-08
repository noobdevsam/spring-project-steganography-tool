package com.example.springprojectsteganographytool.exceptions;

public enum StegoErrorCode {

    VALIDATION_ERROR,
    PAYLOAD_TOO_LARGE,
    INVALID_LSB_DEPTH,
    INVALID_IMAGE_FORMAT,
    ENCRYPTION_KEY_INVALID,
    ENCRYPTION_PROCESS_ERROR,
    METADATA_NOT_FOUND,
    STEGO_DATA_NOT_FOUND,
    STORAGE_ERROR,
    DECODE_FAILURE,
    ENCODE_FAILURE,
    METADATA_DECODING_ERROR,
    STORAGE_SECURITY_ERROR;

    public String typeURI() {
        return "https://api.example.com/errors/" + name();
    }

}
