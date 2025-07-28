package com.example.springprojectsteganographytool.models;

import org.springframework.web.multipart.MultipartFile;

// Used for decoding content from a stego image file
public record StegoDecodeRequestDTO(
        MultipartFile stegoImage,
        String encryptionKey
) {
}
