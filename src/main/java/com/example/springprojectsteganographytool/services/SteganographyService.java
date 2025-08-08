package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.models.StegoDecodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for performing steganography operations such as encoding and decoding
 * messages or files into images, as well as managing encoded data.
 */
public interface SteganographyService {

    /**
     * Encodes a text message into a cover image using the specified LSB depth.
     *
     * @param coverImage The image to be used as the cover for encoding.
     * @param message    The text message to encode.
     * @param password   The password for encrypting the message.
     * @param lsbDepth   The number of least significant bits per channel to use (1 or 2).
     * @return A DTO containing details of the encoding process.
     * @throws Exception If an error occurs during encoding.
     */
    StegoEncodeResponseDTO encodeText(
            BufferedImage coverImage,
            String message,
            String password,
            int lsbDepth // 1 or 2 for LSB depth
    ) throws Exception;

    /**
     * Encodes a file into a cover image using the specified LSB depth.
     *
     * @param coverImage       The image to be used as the cover for encoding.
     * @param originalFileName The original name of the file being encoded.
     * @param fileBytes        The byte array of the file to encode.
     * @param password         The password for encrypting the file.
     * @param lsbDepth         The number of least significant bits per channel to use (1 or 2).
     * @return A DTO containing details of the encoding process.
     * @throws Exception If an error occurs during encoding.
     */
    StegoEncodeResponseDTO encodeFile(
            BufferedImage coverImage,
            String originalFileName,
            byte[] fileBytes,
            String password,
            int lsbDepth
    ) throws Exception;

    /**
     * Decodes a stego image to extract the hidden message or file.
     *
     * @param stegoImage The image containing the hidden data.
     * @param password   The password used to decrypt the hidden data.
     * @return A DTO containing the decoded data.
     * @throws Exception If an error occurs during decoding.
     */
    StegoDecodeResponseDTO decodeProcess(
            BufferedImage stegoImage,
            String password
    ) throws Exception;

    /**
     * Encodes a text message into a byte array representation of the stego image.
     *
     * @param coverImage The image to be used as the cover for encoding.
     * @param message    The text message to encode.
     * @param password   The password for encrypting the message.
     * @param lsbDepth   The number of least significant bits per channel to use (1 or 2).
     * @return A byte array representing the stego image.
     * @throws Exception If an error occurs during encoding.
     */
    byte[] encodeTextToBytes(
            BufferedImage coverImage,
            String message,
            String password,
            int lsbDepth
    ) throws Exception;

    /**
     * Encodes a file into a byte array representation of the stego image.
     *
     * @param coverImage       The image to be used as the cover for encoding.
     * @param originalFileName The original name of the file being encoded.
     * @param fileBytes        The byte array of the file to encode.
     * @param password         The password for encrypting the file.
     * @param lsbDepth         The number of least significant bits per channel to use (1 or 2).
     * @return A byte array representing the stego image.
     * @throws Exception If an error occurs during encoding.
     */
    byte[] encodeFileToBytes(
            BufferedImage coverImage,
            String originalFileName,
            byte[] fileBytes,
            String password,
            int lsbDepth
    ) throws Exception;

    /**
     * Retrieves a list of all encodings performed.
     *
     * @return A list of DTOs containing details of all encodings.
     * @throws Exception If an error occurs while retrieving the encodings.
     */
    List<StegoEncodeResponseDTO> listAllEncodings() throws Exception;

    /**
     * Retrieves the details of a specific encoding by its ID.
     *
     * @param id The unique identifier of the encoding.
     * @return A DTO containing details of the encoding.
     * @throws Exception If an error occurs while retrieving the encoding.
     */
    StegoEncodeResponseDTO getById(UUID id) throws Exception;

    /**
     * Deletes a specific encoding by its ID.
     *
     * @param id The unique identifier of the encoding to delete.
     * @throws Exception If an error occurs while deleting the encoding.
     */
    void deleteById(UUID id) throws Exception;

}