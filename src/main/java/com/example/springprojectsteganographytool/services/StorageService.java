package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.exceptions.data.StorageException;
import com.example.springprojectsteganographytool.exceptions.data.StorageSecurityException;

import java.io.IOException;
import java.nio.file.Path;

public interface StorageService {
    Path save(String relativeFileName, byte[] content) throws
            StorageSecurityException, StorageException, IOException;

    // Returns true if the file was deleted, false if the file did not exist
    boolean delete(String relativeFileName) throws StorageException;
}
