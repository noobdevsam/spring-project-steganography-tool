package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.data.StorageException;
import com.example.springprojectsteganographytool.exceptions.data.StorageSecurityException;
import com.example.springprojectsteganographytool.services.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final Path basePath;

    public StorageServiceImpl(@Value("${app.storage.base-path}") Path basePath) throws IllegalArgumentException {

        if (basePath == null) {
            throw new IllegalArgumentException("app.storage.base-path must not be null");
        }

        this.basePath = basePath.toAbsolutePath().normalize();
    }

    @Override
    public Path save(String relativeFileName, byte[] content) throws StorageException, IOException {
        var targetPath = safeResolve(relativeFileName);
        Files.createDirectories(targetPath.getParent());
        Files.write(targetPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.debug("Saved file: {}  ({} bytes)", targetPath, content.length);
        return targetPath;
    }

    @Override
    public boolean delete(String relativeFileName) throws StorageException {
        Path targetPath = null;
        boolean deleted = false;
        try {
            targetPath = safeResolve(relativeFileName);
            deleted = Files.deleteIfExists(targetPath);
        } catch (IllegalArgumentException | StorageSecurityException | IOException e) {
            throw new StorageException(e.getMessage(), e);
        }

        if (deleted) {
            log.debug("Deleted file: {}", targetPath);
        } else {
            log.warn("Could not delete file (may not exist): {}", targetPath);
        }

        return deleted;
    }

    private Path safeResolve(String name) throws IllegalArgumentException, StorageSecurityException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or blank");
        }

        // allow simple safe name only
        if (!name.matches("^[A-Za-z0-9._-]+$")) {
            throw new IllegalArgumentException("Invalid file name: " + name);
        }

        var targetPath = basePath.resolve(name).normalize();

        if (!targetPath.startsWith(basePath)) {
            throw new StorageSecurityException("Attempt to escape storage directory: ");
        }

        return targetPath;
    }
}
