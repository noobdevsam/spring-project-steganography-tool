package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;
import com.example.springprojectsteganographytool.exceptions.encryption.InvalidEncryptionKeyException;
import com.example.springprojectsteganographytool.services.AesUtilService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class AesUtilServiceImpl implements AesUtilService {

    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256; // AES-256

    @Override
    public byte[] encryptText(String plainText, String key) throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Encryption key is required and cannot be null or blank.");
        }


        try {
            // Convert the plain text to bytes using UTF-8 encoding
            // and then encrypt it using the provided key
            var plaintextBytes = plainText.getBytes(StandardCharsets.UTF_8);

            return encryptBytes(plaintextBytes, key);
        } catch (InvalidEncryptionKeyException | AesKeyInvalidException | AesOperationException exception) {
            throw exception;
        } catch (Exception e) {
            throw new AesOperationException("AES operation failed", e);
        }

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


    private SecretKeySpec deriveKey(
            String password,
            byte[] salt
    ) throws Exception {
        var factory = SecretKeyFactory.getInstance(KDF_ALGORITHM);
        var spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        var keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

}
