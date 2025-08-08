package com.example.springprojectsteganographytool.models;

public record StegoMetadataDTO(
        int lsbDepth, // 1 or 2
        boolean hasText,
        boolean hasFile,
        String encryptionKeyHash, // SHA-256 hash of the AES key
        String originalFileName // Original file name
) {
}
