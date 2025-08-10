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
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * Implementation of the AesUtilService interface providing utility methods for AES encryption and decryption.
 * This service supports text and file encryption/decryption using AES in CBC mode with PKCS5 padding.
 * It also includes methods for generating encryption keys.
 */
@Service
public class AesUtilServiceImpl implements AesUtilService {

    // Constants for encryption configuration
    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256"; // Key derivation function algorithm
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"; // AES with CBC mode and PKCS5 padding
    private static final int ITERATION_COUNT = 65536; // Number of iterations for PBKDF2
    private static final int KEY_LENGTH = 256; // AES-256 key length in bits
    private static final int SALT_LENGTH = 16; // Length of the salt in bytes
    private static final int IV_LENGTH = 16; // Length of the Initialization Vector (IV) in bytes

    private static final SecureRandom RANDOM = new SecureRandom(); // Secure random generator for salt and IV

    /**
     * Encrypts a plain text string using the provided key.
     *
     * @param plainText The plain text to encrypt.
     * @param key       The encryption key.
     * @return The encrypted text as a byte array.
     * @throws InvalidEncryptionKeyException If the key is invalid.
     * @throws AesKeyInvalidException        If the key is null or blank.
     * @throws AesOperationException         If an error occurs during encryption.
     */
    @Override
    public byte[] encryptText(String plainText, String key)
            throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException {

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

    /**
     * Decrypts an encrypted byte array using the provided key.
     *
     * @param cipherBytes The encrypted byte array.
     * @param key         The decryption key.
     * @return The decrypted plain text as a string.
     * @throws InvalidEncryptionKeyException If the key is invalid.
     * @throws AesKeyInvalidException        If the key is null or blank.
     * @throws AesOperationException         If an error occurs during decryption.
     */
    @Override
    public String decryptText(byte[] cipherBytes, String key)
            throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Decryption key is required and cannot be null or blank.");
        }

        try {

            // Decrypt the cipher bytes using the provided key
            // and convert the result back to a String using UTF-8 encoding
            var plainTextBytes = decryptBytes(cipherBytes, key);

            return new String(plainTextBytes, StandardCharsets.UTF_8);

        } catch (InvalidEncryptionKeyException | AesKeyInvalidException | AesOperationException exception) {
            throw exception;
        } catch (Exception e) {
            throw new AesOperationException("AES operation failed", e);
        }
    }

    /**
     * Encrypts a file represented as a byte array using the provided key.
     *
     * @param fileBytes The file content as a byte array.
     * @param key       The encryption key.
     * @return The encrypted file content as a byte array.
     * @throws InvalidEncryptionKeyException If the key is invalid.
     * @throws AesKeyInvalidException        If the key is null or blank.
     * @throws AesOperationException         If an error occurs during encryption.
     */
    @Override
    public byte[] encryptFile(byte[] fileBytes, String key)
            throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Encryption key is required and cannot be null or blank.");
        }

        try {

            // Encrypt the file bytes using the provided key
            return encryptBytes(fileBytes, key);

        } catch (InvalidEncryptionKeyException | AesKeyInvalidException | AesOperationException exception) {
            throw exception;
        } catch (Exception e) {
            throw new AesOperationException("AES file operation failed", e);
        }

    }

    /**
     * Decrypts an encrypted file represented as a byte array using the provided key.
     *
     * @param cipherBytes The encrypted file content as a byte array.
     * @param key         The decryption key.
     * @return The decrypted file content as a byte array.
     * @throws InvalidEncryptionKeyException If the key is invalid.
     * @throws AesKeyInvalidException        If the key is null or blank.
     * @throws AesOperationException         If an error occurs during decryption.
     */
    @Override
    public byte[] decryptFile(byte[] cipherBytes, String key)
            throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Decryption key is required and cannot be null or blank.");
        }

        try {

            // Decrypt the cipher bytes using the provided key
            return decryptBytes(cipherBytes, key);

        } catch (InvalidEncryptionKeyException | AesKeyInvalidException | AesOperationException exception) {
            throw exception;
        } catch (Exception e) {
            throw new AesOperationException("AES file operation failed", e);
        }

    }

    /**
     * Generates a SHA-256 hash of the provided key and returns it as a hex-encoded string.
     *
     * @param key The input key.
     * @return The hex-encoded SHA-256 hash of the key.
     * @throws InvalidEncryptionKeyException If the key is invalid.
     * @throws AesKeyInvalidException        If the key is null or blank.
     */
    @Override
    public String generateKey(String key) throws InvalidEncryptionKeyException, AesKeyInvalidException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Encryption key is required and cannot be null or blank.");
        }

        try {
            var messageDigest = MessageDigest.getInstance("SHA-256");
            var digestKey = messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digestKey); //hex-encoded SHA-256 hash of the key
        } catch (Exception e) {
            throw new AesOperationException("Failed to generate key", e);
        }

    }

    /**
     * Encrypts a byte array using the provided key.
     *
     * @param bytesToEncrypt The byte array to encrypt.
     * @param key            The encryption key.
     * @return The encrypted byte array.
     * @throws Exception If an error occurs during encryption.
     */
    private byte[] encryptBytes(byte[] bytesToEncrypt, String key) throws Exception {
        var salt = new byte[SALT_LENGTH];
        var iv = new byte[IV_LENGTH];
        var cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        var cipherText = cipher.doFinal(bytesToEncrypt);

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

    /**
     * Decrypts a byte array using the provided key.
     *
     * @param bytesToDecrypt The byte array to decrypt.
     * @param key            The decryption key.
     * @return The decrypted byte array.
     * @throws Exception If an error occurs during decryption.
     */
    private byte[] decryptBytes(byte[] bytesToDecrypt, String key) throws Exception {
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

        // Decrypt the cipher text
        return cipher.doFinal(cipherText);
    }

    /**
     * Derives a key using PBKDF2 with the provided password and salt.
     *
     * @param password The password to derive the key from.
     * @param salt     The salt to use in the key derivation.
     * @return The derived key as a SecretKeySpec.
     * @throws Exception If an error occurs during key derivation.
     */
    private SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
        var factory = SecretKeyFactory.getInstance(KDF_ALGORITHM);
        var spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        var keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}