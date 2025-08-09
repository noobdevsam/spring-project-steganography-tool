package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;
import com.example.springprojectsteganographytool.exceptions.encryption.InvalidEncryptionKeyException;

/**
 * Service interface for AES encryption and decryption operations,
 * including text and file handling, as well as key generation.
 */
public interface AesUtilService {

    /**
     * Encrypts the given plain text using the specified key.
     *
     * @param plainText The plain text to encrypt.
     * @param key       The encryption key.
     * @return A byte array containing the encrypted text.
     * @throws InvalidEncryptionKeyException If the provided encryption key is invalid.
     * @throws AesKeyInvalidException        If the AES key is invalid.
     * @throws AesOperationException         If an error occurs during the encryption process.
     */
    byte[] encryptText(
            String plainText,
            String key
    ) throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException;

    /**
     * Decrypts the given cipher bytes using the specified key.
     *
     * @param cipherBytes The byte array representing the encrypted text.
     * @param key         The decryption key.
     * @return The decrypted plain text as a String.
     * @throws InvalidEncryptionKeyException If the provided decryption key is invalid.
     * @throws AesKeyInvalidException        If the AES key is invalid.
     * @throws AesOperationException         If an error occurs during the decryption process.
     */
    String decryptText(
            byte[] cipherBytes,
            String key
    ) throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException;

    /**
     * Encrypts the given file bytes using the specified key.
     *
     * @param fileBytes The byte array representing the file to encrypt.
     * @param key       The encryption key.
     * @return A byte array containing the encrypted file data.
     * @throws InvalidEncryptionKeyException If the provided encryption key is invalid.
     * @throws AesKeyInvalidException        If the AES key is invalid.
     * @throws AesOperationException         If an error occurs during the encryption process.
     */
    byte[] encryptFile(
            byte[] fileBytes,
            String key
    ) throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException;

    /**
     * Decrypts the given cipher bytes of a file using the specified key.
     *
     * @param cipherBytes The byte array representing the encrypted file data.
     * @param key         The decryption key.
     * @return A byte array containing the decrypted file data.
     * @throws InvalidEncryptionKeyException If the provided decryption key is invalid.
     * @throws AesKeyInvalidException        If the AES key is invalid.
     * @throws AesOperationException         If an error occurs during the decryption process.
     */
    byte[] decryptFile(
            byte[] cipherBytes,
            String key
    ) throws InvalidEncryptionKeyException, AesKeyInvalidException, AesOperationException;

    /**
     * Generates a secure encryption key based on the provided input key.
     *
     * @param key The input key to generate a secure encryption key.
     * @return A String representing the generated encryption key.
     * @throws InvalidEncryptionKeyException If the provided input key is invalid.
     * @throws AesKeyInvalidException        If the AES key is invalid.
     */
    String generateKey(
            String key
    ) throws InvalidEncryptionKeyException, AesKeyInvalidException;

}