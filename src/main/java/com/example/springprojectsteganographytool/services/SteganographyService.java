package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.models.StegoDecodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.UUID;

public interface SteganographyService {

    StegoEncodeResponseDTO encodeText(
            BufferedImage coverImage,
            String message,
            String password,
            int lsbDepth // 1 or 2 (bits per channel)
    ) throws Exception;

    StegoEncodeResponseDTO encodeFile(
            BufferedImage coverImage,
            String originalFileName,
            byte[] fileBytes,
            String password,
            int lsbDepth // 1 or 2 (bits per channel)
    ) throws Exception;

    StegoDecodeResponseDTO decodeProcess(
            BufferedImage stegoImage,
            String password
    ) throws Exception;

    byte[] encodeTextToBytes(
            BufferedImage coverImage,
            String message,
            String password,
            int lsbDepth // 1 or 2 (bits per channel)
    ) throws Exception;

    byte[] encodeFileToBytes(
            BufferedImage coverImage,
            String originalFileName,
            byte[] fileBytes,
            String password,
            int lsbDepth // 1 or 2 (bits per channel)
    ) throws Exception;

    List<StegoEncodeResponseDTO> listAllEncodings() throws Exception;

    StegoEncodeResponseDTO getById(UUID id) throws Exception;

    void deleteById(UUID id) throws Exception;

}
