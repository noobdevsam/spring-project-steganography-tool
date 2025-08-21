package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.services.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class StorageServiceImpl implements StorageService {


    private final Path basePath;

    public StorageServiceImpl(@Value("${app.storage.base-path}") Path basePath) {
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

    private Path safeResolve(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or blank");
        }

        // allow simple safe name only
        if (!name.matches("^[A-Za-z0-9._-]+$")) {
            throw new IllegalArgumentException("Invalid file name: " + name);
        }

        var targetPath = basePath.resolve(name).normalize();

        if (!targetPath.startsWith(basePath)) {
            throw new SecurityException("Attempt to escape storage directory: ");
        }

        return targetPath;
    }
}
