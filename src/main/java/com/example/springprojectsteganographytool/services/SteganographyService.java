package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.exceptions.data.StegoDataNotFoundException;
import com.example.springprojectsteganographytool.models.StegoDecodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for performing steganography operations such as encoding and decoding
 * messages or files into images, as well as managing encoded data.
 */
public interface SteganographyService {

    StegoEncodeResponseDTO encodeText(
            BufferedImage coverImage,
            String message,
            String password,
            int lsbDepth
    ) throws Exception;

    StegoEncodeResponseDTO encodeFile(
            BufferedImage coverImage,
            String originalFileName,
            byte[] fileBytes,
            String password,
            int lsbDepth
    ) throws Exception;

    StegoEncodeResponseDTO encodeFileStream(
            BufferedImage coverImage,
            String originalFileName,
            InputStream fileStream,
            long fileSize,
            String password,
            int lsbDepth
    ) throws Exception;

    StegoDecodeResponseDTO decodeProcess(
            BufferedImage stegoImage,
            String password
    ) throws Exception;

    List<StegoEncodeResponseDTO> listAllEncodings();

    StegoEncodeResponseDTO getById(UUID id) throws StegoDataNotFoundException;

    void deleteById(UUID id) throws StegoDataNotFoundException;

}