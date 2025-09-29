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

@Service
@Slf4j
public class LargeFileEncryptionServiceImpl implements LargeFileEncryptionService {

    @Override
    public EncryptedTempFile encryptToTempFile(InputStream plain, String password) throws Exception {

        if (password == null || password.isBlank()) {
            throw new AesKeyInvalidException("Password cannot be null or empty");
        }

        var salt = new byte[AesUtilServiceImpl.SALT_LENGTH];
        var iv = new byte[AesUtilServiceImpl.IV_LENGTH];
        AesUtilServiceImpl.RANDOM.nextBytes(salt);
        AesUtilServiceImpl.RANDOM.nextBytes(iv);

        var key = AesUtilServiceImpl.deriveKey(password, salt);
        var cipher = Cipher.getInstance(AesUtilServiceImpl.CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        var tempFile = Files.createTempFile("enc-", ".bin");
        var success = false;

        try (var fos = Files.newOutputStream(tempFile)) {
            var cipherOutStream = new CipherOutputStream(fos, cipher);

            // write salt + iv first (prefix)
            fos.write(salt);
            fos.write(iv);

            // Stream copy
            plain.transferTo(cipherOutStream);
            cipherOutStream.flush();

            success = true;
        } catch (Exception e) {
            throw new AesOperationException("Streaming encryption failed: " + e.getMessage(), e);
        } finally {
            if (!success) {

                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignore) {
                }

            }
        }

        var length = Files.size(tempFile);
        log.debug("Encrypted large file to temp {} ({} bytes)", tempFile, length);

        return new EncryptedTempFile(tempFile, length);
    }

}
