package com.example.springprojectsteganographytool.services;

import java.nio.file.Path;

public interface StorageService {
    Path save(String relativeFileName, byte[] content) throws Exception;

    void resolve(String relativeFileName) throws Exception;

    // Returns true if the file was deleted, false if the file did not exist
    boolean delete(String relativeFileName) throws Exception;
}
