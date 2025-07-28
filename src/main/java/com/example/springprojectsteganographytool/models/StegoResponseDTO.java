package com.example.springprojectsteganographytool.models;

import java.util.UUID;

// Used for returning a response after processing a stego image
public record StegoResponseDTO(
        UUID id,
        String fileName,
        boolean encrypted,
        String passHint,
        String downloadUrl
) {
}
