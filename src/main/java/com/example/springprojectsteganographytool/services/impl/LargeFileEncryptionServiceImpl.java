package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.models.EncryptedTempFile;
import com.example.springprojectsteganographytool.services.LargeFileEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

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

        return null;
    }

}
