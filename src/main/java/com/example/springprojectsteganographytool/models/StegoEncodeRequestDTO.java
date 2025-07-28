package com.example.springprojectsteganographytool.models;

import org.springframework.web.multipart.MultipartFile;

// used for encoding a message or file into a cover image
public record StegoEncodeRequestDTO(
        MultipartFile coverImage,
        String textMessage,
        MultipartFile fileToHide,
        boolean encrypt,
        String encryptionKey,
        String passHint
) {
}
