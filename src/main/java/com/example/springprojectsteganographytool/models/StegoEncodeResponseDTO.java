package com.example.springprojectsteganographytool.models;

import java.util.UUID;

// Used for returning a response after processing a stego image
public record StegoEncodeResponseDTO(
        UUID id,
        String originalFileName,
        String embeddedFileName,
        boolean hasText,
        boolean hasFile,
        Instant createdDate
) {
}
