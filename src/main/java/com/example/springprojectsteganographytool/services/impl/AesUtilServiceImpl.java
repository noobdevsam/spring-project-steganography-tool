package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;
import com.example.springprojectsteganographytool.exceptions.encryption.InvalidEncryptionKeyException;
import com.example.springprojectsteganographytool.services.AesUtilService;
import org.springframework.stereotype.Service;

@Service
public class AesUtilServiceImpl implements AesUtilService {
    @Override
    public byte[] encryptText(String plainText, String key) throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException {
        return new byte[0];
    }

    @Override
    public String decryptText(byte[] cipherBytes, String key) throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException {
        return "";
    }

    @Override
    public byte[] encryptFile(byte[] fileBytes, String key) throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException {
        return new byte[0];
    }

    @Override
    public byte[] decryptFile(byte[] cipherBytes, String key) throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException {
        return new byte[0];
    }

    @Override
    public String generateKey(String key) throws InvalidEncryptionKeyException, AesKeyInvalidException {
        return "";
    }
}
