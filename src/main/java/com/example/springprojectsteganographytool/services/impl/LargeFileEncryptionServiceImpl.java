package com.example.springprojectsteganographytool.services.impl;

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
        return null;
    }

}
