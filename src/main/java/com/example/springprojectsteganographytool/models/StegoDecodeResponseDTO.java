package com.example.springprojectsteganographytool.models;

// Used for returning after decoding is successful
public record StegoDecodeResponseDTO(
        String message,
        String embeddedFileName,
        byte[] embeddedFileContent,
        boolean hasText,
        boolean hasFile
) {
}
