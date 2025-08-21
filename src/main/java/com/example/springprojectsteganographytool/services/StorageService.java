package com.example.springprojectsteganographytool.services;

import java.nio.file.Path;

public interface StorageService {
    Path save(String relativeFileName, byte[] content) throws Exception;

    Path resolve(String relativeFileName) throws Exception;
}
