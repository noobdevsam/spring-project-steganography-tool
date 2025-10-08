package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;
import com.example.springprojectsteganographytool.models.EncryptedTempFile;
import com.example.springprojectsteganographytool.services.LargeFileEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Service implementation for encrypting large files using AES encryption.
 * This class provides functionality to encrypt an input stream and store the result
 * in a temporary file.
 */
@Service
@Slf4j
public class LargeFileEncryptionServiceImpl implements LargeFileEncryptionService {

    /**
     * Encrypts a large file stream using AES encryption and writes the encrypted data
     * to a temporary file. The encryption process includes generating a random salt
     * and initialization vector (IV), deriving a key from the provided password, and
     * streaming the encrypted data to the output file.
     *
     * @param plain    The input stream of the plaintext file to be encrypted.
     * @param password The password used to derive the encryption key.
     * @return An `EncryptedTempFile` object containing the path to the encrypted file
     * and its size in bytes.
     * @throws AesKeyInvalidException If the provided password is null or empty.
     * @throws AesOperationException  If an error occurs during the encryption process.
     * @throws Exception              If an unexpected error occurs.
     */
    @Override
    public EncryptedTempFile encryptToTempFile(InputStream plain, String password) throws Exception {

        log.debug("Encrypting large file stream with password");

        // Validate the password
        if (password == null || password.isBlank()) {
            throw new AesKeyInvalidException("Password cannot be null or empty");
        }

        // Generate random salt and IV
        var salt = new byte[AesUtilServiceImpl.SALT_LENGTH];
        var iv = new byte[AesUtilServiceImpl.IV_LENGTH];
        AesUtilServiceImpl.RANDOM.nextBytes(salt);
        AesUtilServiceImpl.RANDOM.nextBytes(iv);

        // Derive the encryption key
        var key = AesUtilServiceImpl.deriveKey(password, salt);
        var cipher = Cipher.getInstance(AesUtilServiceImpl.CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        // Create a temporary file to store the encrypted data
        var tempFile = Files.createTempFile("enc-", ".bin");
        var success = false;

        try (var fos = Files.newOutputStream(tempFile)) {
            var cipherOutStream = new CipherOutputStream(fos, cipher);

            // Write salt and IV as a prefix to the encrypted file
            fos.write(salt);
            fos.write(iv);

            // Stream the plaintext data to the cipher output stream
            plain.transferTo(cipherOutStream);

            // Ensure all data is flushed and finalize encryption
            cipherOutStream.flush();
            cipherOutStream.close(); // Closing triggers doFinal() in CipherOutputStream
            success = true;
        } catch (Exception e) {
            // Handle encryption errors
            throw new AesOperationException("Streaming encryption failed: " + e.getMessage(), e);
        } finally {
            if (!success) {
                // Clean up the temporary file if encryption fails
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignore) {
                }
            }
        }

        // Log the result and return the encrypted file details
        var length = Files.size(tempFile);
        log.debug("Encrypted large file to temp {} ({} bytes)", tempFile, length);

        return new EncryptedTempFile(tempFile, length);
    }

}