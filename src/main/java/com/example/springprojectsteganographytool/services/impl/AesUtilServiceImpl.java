package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;
import com.example.springprojectsteganographytool.exceptions.encryption.InvalidEncryptionKeyException;
import com.example.springprojectsteganographytool.services.AesUtilService;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@Service
public class AesUtilServiceImpl implements AesUtilService {

    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"; // AES with CBC mode and PKCS5 padding
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256; // AES-256
    private static final int SALT_LENGTH = 16; // Length of the salt in bytes
    private static final int IV_LENGTH = 16; // Length of the Initialization Vector (IV) in bytes

    private static final SecureRandom RANDOM = new SecureRandom();


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

    // ------ Helper Methods ------

    private byte[] encryptBytes(byte[] plaintextBytes, String key) throws Exception {

        var salt = new byte[SALT_LENGTH];
        var iv = new byte[IV_LENGTH];
        var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        var cipherText = cipher.doFinal(plaintextBytes);

        // Generate random salt and IV
        RANDOM.nextBytes(salt);
        RANDOM.nextBytes(iv);

        // Derive the key using PBKDF2 with the provided key and generated salt
        var keySpec = deriveKey(key, salt);

        // Initialize the cipher with the derived key and generated IV
        cipher.init(
                Cipher.ENCRYPT_MODE,
                keySpec,
                new IvParameterSpec(iv)
        );

        // Combine salt, IV, and cipher text into a single byte array
        var outputBytes = new byte[SALT_LENGTH + IV_LENGTH + cipherText.length];

        // Copy salt, IV, and cipher text into the output byte array
        System.arraycopy(salt, 0, outputBytes, 0, SALT_LENGTH); // Copy salt
        System.arraycopy(iv, 0, outputBytes, SALT_LENGTH, IV_LENGTH); // Copy IV
        System.arraycopy(cipherText, 0, outputBytes, SALT_LENGTH + IV_LENGTH, cipherText.length); // Copy cipher text

        return outputBytes;

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
