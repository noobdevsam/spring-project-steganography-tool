package com.example.springprojectsteganographytool.models;

// Used for returning after decoding is successful
public record StegoDecodeResponseDTO(
        String message,
        String embeddedFileName,
        boolean hasText,
        boolean hasFile
) {
}
