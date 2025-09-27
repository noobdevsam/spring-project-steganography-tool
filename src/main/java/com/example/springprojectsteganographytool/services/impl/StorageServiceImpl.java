package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.services.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final Path basePath;

    public StorageServiceImpl(@Value("${app.storage.base-path}") Path basePath) {

        if (basePath == null) {
            throw new IllegalArgumentException("app.storage.base-path must not be null");
        }

        this.basePath = basePath.toAbsolutePath().normalize();
    }

    @Override
    public Path save(String relativeFileName, byte[] content) throws Exception {
        var targetPath = safeResolve(relativeFileName);
        Files.createDirectories(targetPath.getParent());
        Files.write(targetPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.debug("Saved file: {}  ({} bytes)", targetPath, content.length);
        return targetPath;
    }

    @Override
    public Path resolve(String relativeFileName) throws Exception {
        var targetPath = safeResolve(relativeFileName);

        if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            throw new FileNotFoundException("File not found: " + relativeFileName);
        }

        return targetPath;
    }

    @Override
    public boolean delete(String relativeFileName) throws Exception {
        var targetPath = safeResolve(relativeFileName);
        var deleted = Files.deleteIfExists(targetPath);

        if (deleted) {
            log.debug("Deleted file: {}", targetPath);
        } else {
            log.warn("Could not delete file (may not exist): {}", targetPath);
        }

        return deleted;
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
