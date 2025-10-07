package com.example.springprojectsteganographytool.models;

import java.util.UUID;

// Used for returning a response after encoding data into an image
// Excludes sensitive information like encryptionKeyHash
// and focuses on metadata about the stego file and embedded data
public record StegoEncodeResponseDTO(
        UUID id,
        String coverImageName,
        String fileNameOfEmbeddedData,
        String stegoFileName,
        Long stegoFileSize,
        boolean hasText,
        boolean hasFile
) {
}
