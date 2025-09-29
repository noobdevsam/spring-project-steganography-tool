package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.models.EncryptedTempFile;

import java.io.InputStream;

/**
 * Service for streaming AES encryption of large payloads into a temporary file.
 * The encrypted file layout matches the in-memory AES format:
 * [salt(16)][iv(16)][cipherText...]
 */
public interface LargeFileEncryptionService {

    /**
     * Encrypts the given plaintext stream into a temp file using the provided password.
     * The caller is responsible for deleting the temp file after use.
     */
    EncryptedTempFile encryptToTempFile(
            InputStream plain,
            String password
    ) throws Exception;

}
