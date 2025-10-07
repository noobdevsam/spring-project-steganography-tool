package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;
import com.example.springprojectsteganographytool.services.AesUtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;

@Service
@Slf4j
public class AesUtilServiceImpl implements AesUtilService {

    static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"; // AES with CBC mode and PKCS5 padding
    static final int SALT_LENGTH = 16; // Length of the salt in bytes
    static final int IV_LENGTH = 16; // Length of the Initialization Vector (IV) in bytes
    static final SecureRandom RANDOM = new SecureRandom(); // Secure random generator for salt and IV
    // Constants for encryption configuration
    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256"; // Key derivation function algorithm
    private static final int ITERATION_COUNT = 65536; // Number of iterations for PBKDF2
    private static final int KEY_LENGTH = 256; // AES-256 key length in bits

    public AesUtilServiceImpl() {
    }

    static SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
        var factory = SecretKeyFactory.getInstance(KDF_ALGORITHM);
        var spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        var keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public byte[] encryptText(String plainText, String key)
            throws AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Encryption key is required and cannot be null or blank.");
        }

        try {
            log.info("Encrypting text using AES...");
            return encryptBytes(
                    plainText.getBytes(StandardCharsets.UTF_8), key
            );
        } catch (Exception ee) {
            throw new AesOperationException("AES operation failed", ee);
        }
    }

    @Override
    public String decryptText(byte[] cipherBytes, String key)
            throws AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Decryption key is required and cannot be null or blank.");
        }

        try {
            log.info("Decrypting text using AES...");
            var plainTextBytes = decryptBytes(
                    cipherBytes, key
            );
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new AesOperationException("AES decryption operation failed", exception);
        }
    }

    @Override
    public byte[] encryptFile(byte[] fileBytes, String key)
            throws AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Encryption key is required and cannot be null or blank.");
        }

        try {
            log.info("Encrypting file using AES...");
            return encryptBytes(fileBytes, key);
        } catch (Exception ee) {
            throw new AesOperationException("AES file encryption operation failed", ee);
        }

    }

    @Override
    public byte[] decryptFile(byte[] cipherBytes, String key)
            throws AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Decryption key is required and cannot be null or blank.");
        }

        try {
            log.info("Decrypting file using AES...");
            return decryptBytes(cipherBytes, key);
        } catch (Exception ee) {
            throw new AesOperationException("AES file decryption operation failed", ee);
        }

    }

    @Override
    public String generateKey(String key) throws AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Encryption key is required and cannot be null or blank.");
        }

        try {
            log.info("Generating hash of the key...");

            // Generate an SHA-256 hash of the key
            var messageDigest = MessageDigest.getInstance("SHA-256");

            // Convert the key to bytes and compute the digest
            var digestKey = messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));

            // Return the hex-encoded representation of the digest.
            // Hex-encoded SHA-256 hash of the key
            return HexFormat.of().formatHex(digestKey);
        } catch (Exception ee) {
            throw new AesOperationException("Key generation operation failed: " + ee.getMessage(), ee);
        }

    }

    // ----- Private Helper Methods -----

    private byte[] encryptBytes(byte[] bytesToEncrypt, String key) throws Exception {

        log.info("Starting AES encryption process...");

        var salt = new byte[SALT_LENGTH];
        var iv = new byte[IV_LENGTH];
        var cipher = Cipher.getInstance(CIPHER_ALGORITHM);

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

        var cipherText = cipher.doFinal(bytesToEncrypt);

        // Combine salt, IV, and cipher text into a single byte array
        var outputBytes = new byte[SALT_LENGTH + IV_LENGTH + cipherText.length];

        // Copy salt, IV, and cipher text into the output byte array
        System.arraycopy(salt, 0, outputBytes, 0, SALT_LENGTH); // Copy salt
        System.arraycopy(iv, 0, outputBytes, SALT_LENGTH, IV_LENGTH); // Copy IV
        System.arraycopy(cipherText, 0, outputBytes, SALT_LENGTH + IV_LENGTH, cipherText.length); // Copy cipher text

        log.info("AES encryption process completed.");

        return outputBytes;
    }

    private byte[] decryptBytes(byte[] bytesToDecrypt, String key) throws Exception {

        log.info("Starting AES decryption process...");

        if (bytesToDecrypt == null || bytesToDecrypt.length < SALT_LENGTH + IV_LENGTH) {
            throw new AesOperationException("Invalid input for decryption.");
        }

        // Extract salt and IV from the input byte array
        var salt = Arrays.copyOfRange(bytesToDecrypt, 0, SALT_LENGTH);
        var iv = Arrays.copyOfRange(bytesToDecrypt, SALT_LENGTH, SALT_LENGTH + IV_LENGTH);
        var cipherText = Arrays.copyOfRange(bytesToDecrypt, SALT_LENGTH + IV_LENGTH, bytesToDecrypt.length);


        // Derive the key using PBKDF2 with the provided key and extracted salt
        var keySpec = deriveKey(key, salt);

        // Initialize the cipher for decryption
        var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(
                Cipher.DECRYPT_MODE,
                keySpec,
                new IvParameterSpec(iv)
        );

        log.info("AES decryption process completed.");

        // Decrypt the cipher text
        return cipher.doFinal(cipherText);
    }

}