package com.example.springprojectsteganographytool.models;

import java.util.UUID;

// Used for decoding content from a stego image file
public record StegoDecodeRequestDTO(
        UUID id,
        String password
) {
}
