package com.example.springprojectsteganographytool.services;

/**
 * Service interface for AES encryption and decryption operations.
 */
public interface AesUtilService {

    /**
     * Encrypts the given plain text using the specified password.
     * Returns: salt || iv || ciphertext
     *
     * @param plainText The byte array representing the plain text to encrypt.
     * @param password  The password used for encryption.
     * @return A byte array containing the encrypted data.
     * @throws Exception If an error occurs during encryption.
     */
    byte[] encrypt(
            byte[] plainText,
            String password
    ) throws Exception;

    /**
     * Decrypts the given input data using the specified password.
     * Decrypts data formatted as: salt || iv || ciphertext
     *
     * @param input    The byte array representing the encrypted data to decrypt.
     * @param password The password used for decryption.
     * @return A byte array containing the decrypted data.
     * @throws Exception If an error occurs during decryption.
     */
    byte[] decrypt(
            byte[] input,
            String password
    ) throws Exception;

}