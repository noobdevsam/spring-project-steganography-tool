package com.example.springprojectsteganographytool.models;

import java.time.Instant;
import java.util.UUID;

public record StegoMetadataDTO(
        UUID id,
        String originalFileName,
        String embeddedFileName,
        boolean hasText,
        boolean hasFile,
        Instant createdDate
) {
}
