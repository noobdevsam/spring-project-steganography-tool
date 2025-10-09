package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.exceptions.data.StorageException;
import com.example.springprojectsteganographytool.exceptions.data.StorageFileNotFoundException;
import com.example.springprojectsteganographytool.exceptions.data.StorageSecurityException;
import com.example.springprojectsteganographytool.services.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Implementation of the StorageService interface.
 * Provides methods to save and delete files in a secure manner.
 */
@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final Path basePath;

    /**
     * Constructor for StorageServiceImpl.
     *
     * @param basePath the base directory for file storage, injected from the application properties.
     * @throws IllegalArgumentException if the basePath is null.
     */
    public StorageServiceImpl(@Value("${app.storage.base-path}") Path basePath) throws IllegalArgumentException {
        if (basePath == null) {
            throw new IllegalArgumentException("app.storage.base-path must not be null");
        }

        this.basePath = basePath.toAbsolutePath().normalize();

        // **KEY CHANGE: Ensure the base directory exists**
        try {
            Files.createDirectories(this.basePath);
            log.info("Using storage base path: {}", this.basePath);
        } catch (IOException e) {
            log.error("Failed to create storage base path: {}", this.basePath, e);
            throw new IllegalArgumentException("Cannot create storage directory: " + this.basePath, e);
        }
    }

    /**
     * Saves a file to the storage directory.
     *
     * @param relativeFileName the relative name of the file to save.
     * @param content          the content of the file as a byte array.
     * @return the path to the saved file.
     * @throws StorageSecurityException if an attempt is made to escape the storage directory.
     * @throws StorageException         if an error occurs during file operations.
     */
    @Override
    public Path save(String relativeFileName, byte[] content) throws StorageSecurityException, StorageException {
        log.debug("Saving file: {} ({} bytes)", relativeFileName, content.length);

        try {
            var targetPath = safeResolve(relativeFileName);
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.debug("Saved file: {}  ({} bytes)", targetPath, content.length);
            return targetPath;
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + relativeFileName, e);
        }
    }

    /**
     * Deletes a file from the storage directory.
     *
     * @param relativeFileName the relative name of the file to delete.
     * @return true if the file was successfully deleted, false otherwise.
     * @throws StorageException if an error occurs during file operations.
     */
    @Override
    public boolean delete(String relativeFileName) throws StorageException {
        log.debug("Deleting file: {}", relativeFileName);

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

    /**
     * Loads a file as a Spring `Resource` object.
     * <p>
     * This method resolves the given relative file name to an absolute path within the storage directory,
     * and attempts to load it as a `UrlResource`. If the file exists and is readable, the resource is returned.
     * Otherwise, a `StorageFileNotFoundException` is thrown.
     *
     * @param relativeFileName the relative name of the file to load.
     * @return the file as a `Resource` object.
     * @throws StorageException             if the file cannot be resolved or loaded.
     * @throws StorageFileNotFoundException if the file does not exist or is not readable.
     */
    @Override
    public Resource loadAsResource(String relativeFileName) throws StorageException {
        try {
            // Resolve the relative file name to an absolute path within the storage directory
            var filePath = safeResolve(relativeFileName);
            // Create a UrlResource from the resolved file path
            var resource = new UrlResource(filePath.toUri());

            // Check if the resource exists and is readable
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                // Throw an exception if the file cannot be read
                throw new StorageFileNotFoundException("Could not read file: " + relativeFileName);
            }

        } catch (MalformedURLException e) {
            // Handle malformed URL exceptions and wrap them in a StorageFileNotFoundException
            throw new StorageFileNotFoundException("Could not read file: " + relativeFileName, e);
        }
    }

    /**
     * Resolves a file name to an absolute path within the storage directory.
     *
     * @param name the relative file name to resolve.
     * @return the resolved absolute path.
     * @throws IllegalArgumentException if the file name is null, blank, or invalid.
     * @throws StorageSecurityException if the resolved path attempts to escape the storage directory.
     */
    private Path safeResolve(String name) throws IllegalArgumentException, StorageSecurityException {
        log.debug("Resolving file name: {}", name);

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