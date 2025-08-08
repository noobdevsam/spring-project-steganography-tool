package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.models.StegoMetadataDTO;

/**
 * Service interface for performing LSB (Least Significant Bit) steganography operations,
 * including encoding and decoding messages or files, as well as extracting metadata.
 */
public interface LsbUtilService {

    /**
     * Encodes a message into an image using LSB steganography.
     *
     * @param imageBytes   The byte array representing the image to encode the message into.
     * @param messageBytes The byte array representing the message to encode.
     * @param metadata     The metadata containing encoding details such as LSB depth.
     * @return A byte array representing the stego image with the encoded message.
     * @throws Exception If an error occurs during the encoding process.
     */
    byte[] encodeMessage(
            byte[] imageBytes,
            byte[] messageBytes,
            StegoMetadataDTO metadata
    ) throws Exception;

    /**
     * Decodes a message from a stego image using LSB steganography.
     * Metadata must be extracted beforehand to know lsbDepth.
     *
     * @param stegoImageBytes The byte array representing the stego image containing the encoded message.
     * @param lsbDepth        The number of least significant bits per channel used for encoding.
     * @return A byte array representing the decoded message.
     * @throws Exception If an error occurs during the decoding process.
     */
    byte[] decodeMessage(
            byte[] stegoImageBytes,
            int lsbDepth
    ) throws Exception;

    /**
     * Encodes a file into an image using LSB steganography.
     *
     * @param imageBytes The byte array representing the image to encode the file into.
     * @param fileBytes  The byte array representing the file to encode.
     * @param metadata   The metadata containing encoding details such as LSB depth.
     * @return A byte array representing the stego image with the encoded file.
     * @throws Exception If an error occurs during the encoding process.
     */
    byte[] encodeFile(
            byte[] imageBytes,
            byte[] fileBytes,
            StegoMetadataDTO metadata
    ) throws Exception;

    /**
     * Decodes a file from a stego image using LSB steganography.
     *
     * @param stegoImageBytes The byte array representing the stego image containing the encoded file.
     * @param lsbDepth        The number of least significant bits per channel used for encoding.
     * @return A byte array representing the decoded file.
     * @throws Exception If an error occurs during the decoding process.
     */
    byte[] decodeFile(
            byte[] stegoImageBytes,
            int lsbDepth
    ) throws Exception;

    /**
     * Extracts metadata from a stego image without decoding payload data.
     *
     * @param stegoImageBytes The byte array representing the stego image.
     * @return A DTO containing the extracted metadata, such as encoding details.
     * @throws Exception If an error occurs during metadata extraction.
     */
    StegoMetadataDTO extractMetadata(
            byte[] stegoImageBytes
    ) throws Exception;

}