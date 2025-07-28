package com.example.springprojectsteganographytool.models;

// Used for returning after decoding is successful
public record StegoDecodedContentDTO(
        String extractedText,
        byte[] extractedFile,
        String originalFileName
) {
}
