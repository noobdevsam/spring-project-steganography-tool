package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.services.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path basePath;

    public StorageServiceImpl(@Value("${storage.base-path}") Path basePath) {
        this.basePath = basePath.toAbsolutePath().normalize();
    }

    @Override
    public Path save(String relativeFileName, byte[] content) throws Exception {
        return null;
    }

    @Override
    public Path resolve(String relativeFileName) throws Exception {
        return null;
    }
}
