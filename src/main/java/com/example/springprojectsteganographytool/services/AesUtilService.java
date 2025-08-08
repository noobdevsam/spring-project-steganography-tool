package com.example.springprojectsteganographytool.services;

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