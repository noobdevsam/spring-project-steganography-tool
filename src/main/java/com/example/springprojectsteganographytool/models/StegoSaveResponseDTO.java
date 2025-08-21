package com.example.springprojectsteganographytool.models;

import java.time.Instant;

public record StegoSaveResponseDTO(
        String fileName,
        String absolutePath,
        String url,
        long size,
        Instant savedAt
) {
}
