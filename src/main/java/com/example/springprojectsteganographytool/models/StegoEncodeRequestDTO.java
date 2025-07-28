package com.example.springprojectsteganographytool.models;

import org.springframework.web.multipart.MultipartFile;

public record StegoEncodeRequestDTO(
        MultipartFile coverImage,
        String textMessage,
        MultipartFile fileToHide,
        boolean encrypt,
        String encryptionKey,
        String passHint
) {
}
