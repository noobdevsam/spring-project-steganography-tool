package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;

/**
 * Service interface for AES encryption and decryption operations,
 * including text and file handling, as well as key generation.
 */
public interface AesUtilService {

    byte[] encryptText(
            String plainText,
            String key
    ) throws AesKeyInvalidException, AesOperationException;

    String decryptText(
            byte[] cipherBytes,
            String key
    ) throws AesKeyInvalidException, AesOperationException;

    byte[] encryptFile(
            byte[] fileBytes,
            String key
    ) throws AesKeyInvalidException, AesOperationException;

    byte[] decryptFile(
            byte[] cipherBytes,
            String key
    ) throws AesKeyInvalidException, AesOperationException;

    String generateKey(
            String key
    ) throws AesOperationException;

}