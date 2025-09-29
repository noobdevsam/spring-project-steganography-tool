package com.example.springprojectsteganographytool.services;

/**
 * Service interface for AES encryption and decryption operations,
 * including text and file handling, as well as key generation.
 */
public interface AesUtilService {

    byte[] encryptText(
            String plainText,
            String key
    ) throws Exception;

    String decryptText(
            byte[] cipherBytes,
            String key
    ) throws Exception;

    byte[] encryptFile(
            byte[] fileBytes,
            String key
    ) throws Exception;

    byte[] decryptFile(
            byte[] cipherBytes,
            String key
    ) throws Exception;

    String generateKey(
            String key
    ) throws Exception;

}