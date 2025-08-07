package com.example.springprojectsteganographytool.models;

public record StegoDownloadDTO(
        String fileName,
        String contentType,
        byte[] fileData
) {
}
