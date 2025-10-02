package com.example.springprojectsteganographytool.models;

// Used for returning after decoding is successful
public record StegoDecodeResponseDTO(
        boolean hasText,
        boolean hasFile,
        String message,
        String extractedFileName,
        Long extractedFileSize,
        String extractedFieAbsolutePath,
        long createdAtEpochMillis,
        long expiredAtEpochMillis
) {
}
