package com.example.springprojectsteganographytool.models;

import org.springframework.web.multipart.MultipartFile;

// used for encoding a message or file into a cover image
public record StegoEncodeRequestDTO(
        MultipartFile coverImage,
        MultipartFile embeddedFile,
        String message,
        String password
) {
}
