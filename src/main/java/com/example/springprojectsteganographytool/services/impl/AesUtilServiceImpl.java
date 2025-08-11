package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;
import com.example.springprojectsteganographytool.services.AesUtilService;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

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
    private final ExecutorService executorService;

    public AesUtilServiceImpl(@Qualifier("virtualThreadExecutor") ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Encrypts a plain text string using the provided key.
     * This method validates the key, encrypts the plain text asynchronously using an executor service,
     * and returns the encrypted byte array.
     *
     * @param plainText The plain text to encrypt.
     * @param key       The encryption key.
     * @return The encrypted text as a byte array.
     * @throws AesKeyInvalidException If the key is null or blank.
     * @throws AesOperationException  If an error occurs during encryption or task execution.
     */
    @Override
    public byte[] encryptText(String plainText, String key)
            throws AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Encryption key is required and cannot be null or blank.");
        }

        // Create a callable task to perform the encryption
        Callable<byte[]> task = () -> encryptBytes(
                plainText.getBytes(StandardCharsets.UTF_8), key
        );

        try {
            // Submit the encryption task to the executor service and wait for the result
            // The get() method blocks until the task is complete and retrieves the result
            return executorService.submit(task).get();
        } catch (InterruptedException interruptedException) {
            // Restore the interrupted status and throw an exception
            Thread.currentThread().interrupt();
            throw new AesOperationException("AES encryption interrupted", interruptedException);
        } catch (ExecutionException ee) {
            // Handle the cause of the execution exception
            handleExecutionCause(ee);
            throw new AesOperationException("AES operation failed", ee);
        }
    }

    /**
     * Decrypts an encrypted byte array using the provided key.
     * This method validates the decryption key, processes the decryption asynchronously using an executor service,
     * and converts the decrypted byte array back to a plain text string.
     *
     * @param cipherBytes The encrypted byte array to decrypt.
     * @param key         The decryption key to use for decryption.
     * @return The decrypted plain text as a string.
     * @throws AesKeyInvalidException If the provided decryption key is null or blank.
     * @throws AesOperationException  If an error occurs during decryption or task execution.
     */
    @Override
    public String decryptText(byte[] cipherBytes, String key)
            throws AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Decryption key is required and cannot be null or blank.");
        }

        // Create a callable task to perform the decryption
        Callable<byte[]> task = () -> decryptBytes(
                cipherBytes, key
        );

        try {

            // Submit the decryption task to the executor service and wait for the result
            // Convert the decrypted byte array back to a string using UTF-8 encoding
            var plainTextBytes = executorService.submit(task).get();

            return new String(plainTextBytes, StandardCharsets.UTF_8);

        } catch (InterruptedException exception) {
            // Restore the interrupted status and throw an exception
            Thread.currentThread().interrupt();
            throw new AesOperationException("AES decryption interrupted", exception);
        } catch (ExecutionException ee) {
            // Handle the cause of the execution exception
            handleExecutionCause(ee);
            throw new AesOperationException("AES decryption operation failed", ee);
        }
    }

    /**
     * Encrypts a file represented as a byte array using the provided key.
     * This method validates the encryption key, processes the encryption asynchronously using an executor service,
     * and returns the encrypted file content as a byte array.
     *
     * @param fileBytes The file content to encrypt, represented as a byte array.
     * @param key       The encryption key to use for encrypting the file.
     * @return A byte array containing the encrypted file content.
     * @throws AesKeyInvalidException If the provided encryption key is null or blank.
     * @throws AesOperationException  If an error occurs during the encryption process or task execution.
     */
    @Override
    public byte[] encryptFile(byte[] fileBytes, String key)
            throws AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Encryption key is required and cannot be null or blank.");
        }

        // Create a callable task to perform the encryption
        Callable<byte[]> task = () -> encryptBytes(fileBytes, key);

        try {
            // Submit the encryption task to the executor service and wait for the result
            return executorService.submit(task).get();
        } catch (InterruptedException interruptedException) {
            // Restore the interrupted status and throw an exception
            Thread.currentThread().interrupt();
            throw new AesOperationException("AES file encryption interrupted", interruptedException);
        } catch (ExecutionException ee) {
            // Handle the cause of the execution exception
            handleExecutionCause(ee);
            throw new AesOperationException("AES file encryption operation failed", ee);
        }

    }

    /**
     * Decrypts an encrypted file represented as a byte array using the provided key.
     * This method validates the decryption key, processes the decryption asynchronously using an executor service,
     * and returns the decrypted file content as a byte array.
     *
     * @param cipherBytes The encrypted file content as a byte array.
     * @param key         The decryption key to use for decryption.
     * @return A byte array containing the decrypted file content.
     * @throws AesKeyInvalidException If the provided decryption key is null or blank.
     * @throws AesOperationException  If an error occurs during the decryption process or task execution.
     */
    @Override
    public byte[] decryptFile(byte[] cipherBytes, String key)
            throws AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Decryption key is required and cannot be null or blank.");
        }

        // Create a callable task to perform the decryption
        Callable<byte[]> task = () -> decryptBytes(cipherBytes, key);

        try {
            // Submit the decryption task to the executor service and wait for the result
            return executorService.submit(task).get();
        } catch (InterruptedException interruptedException) {
            // Restore the interrupted status and throw an exception
            Thread.currentThread().interrupt();
            throw new AesOperationException("AES file decryption interrupted", interruptedException);
        } catch (ExecutionException ee) {
            // Handle the cause of the execution exception
            handleExecutionCause(ee);
            throw new AesOperationException("AES file decryption operation failed", ee);
        }

    }

    /**
     * Generates an SHA-256 hash of the provided key and returns it as a hex-encoded string.
     * This method validates the input key, processes the hash generation asynchronously using an executor service,
     * and returns the resulting hash in a hex-encoded format.
     *
     * @param key The input key to be hashed.
     * @return The hex-encoded SHA-256 hash of the input key.
     * @throws AesKeyInvalidException If the input key is null or blank.
     * @throws AesOperationException  If an error occurs during the hash generation process.
     */
    @Override
    public String generateKey(String key) throws AesKeyInvalidException, AesOperationException {

        // Validate the key
        if (key == null || key.isBlank()) {
            throw new AesKeyInvalidException("Encryption key is required and cannot be null or blank.");
        }

        Callable<String> task = () -> {
            // Generate an SHA-256 hash of the key
            var messageDigest = MessageDigest.getInstance("SHA-256");

            // Convert the key to bytes and compute the digest
            var digestKey = messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));

            // Return the hex-encoded representation of the digest.
            // Hex-encoded SHA-256 hash of the key
            return HexFormat.of().formatHex(digestKey);
        };

        try {
            // Submit the key generation task to the executor service and wait for the result
            return executorService.submit(task).get();
        } catch (InterruptedException interruptedException) {
            // Restore the interrupted status and throw an exception
            Thread.currentThread().interrupt();
            throw new AesOperationException("Key generation interrupted", interruptedException);
        } catch (ExecutionException ee) {
            throw new AesOperationException("Key generation operation failed", ee);
        }

    }


    // ----- Private Helper Methods -----

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

    /**
     * Handles the cause of an ExecutionException by rethrowing it as a specific exception
     * or wrapping it in a generic AesOperationException.
     *
     * @param ee The ExecutionException to handle.
     * @throws AesKeyInvalidException If the cause is an AesKeyInvalidException.
     * @throws AesOperationException  If the cause is an AesOperationException or any other exception.
     */
    private void handleExecutionCause(ExecutionException ee) throws AesKeyInvalidException, AesOperationException {
        var cause = ee.getCause();

        if (cause instanceof AesKeyInvalidException) {
            throw (AesKeyInvalidException) cause;
        }

        if (cause instanceof AesOperationException) {
            throw (AesOperationException) cause;
        }

        // Otherwise, wrap the cause in a generic AesOperationException
        throw new AesOperationException("Unexpected error during AES operation", cause);
    }
}