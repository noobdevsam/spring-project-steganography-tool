package com.example.springprojectsteganographytool.models;

import java.nio.file.Path;

public record EncryptedTempFile(
        Path path,
        long length
) {
}
