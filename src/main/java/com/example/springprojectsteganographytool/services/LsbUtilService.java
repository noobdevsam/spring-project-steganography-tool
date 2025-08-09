package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.exceptions.data.MessageTooLargeException;
import com.example.springprojectsteganographytool.exceptions.data.StegoDataNotFoundException;
import com.example.springprojectsteganographytool.exceptions.file.InvalidImageFormatException;
import com.example.springprojectsteganographytool.exceptions.lsb.InvalidLsbDepthException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbDecodingException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbEncodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataDecodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataNotFoundException;
import com.example.springprojectsteganographytool.models.StegoMetadataDTO;

/**
 * Service interface for performing LSB (Least Significant Bit) steganography operations.
 * Provides methods for encoding and decoding messages or files into images, as well as
 * extracting metadata from stego images.
 */
public interface LsbUtilService {

    /**
     * Encodes a message into an image using LSB steganography.
     *
     * @param imageBytes   The byte array representing the image to encode the message into.
     * @param messageBytes The byte array representing the message to encode.
     * @param metadata     The metadata containing encoding details such as LSB depth.
     * @return A byte array representing the stego image with the encoded message.
     * @throws InvalidLsbDepthException    If the specified LSB depth is invalid.
     * @throws MessageTooLargeException    If the message is too large to fit in the image.
     * @throws LsbEncodingException        If an error occurs during the encoding process.
     * @throws InvalidImageFormatException If the provided image format is invalid.
     */
    byte[] encodeMessage(
            byte[] imageBytes,
            byte[] messageBytes,
            StegoMetadataDTO metadata
    ) throws InvalidLsbDepthException, MessageTooLargeException, LsbEncodingException, InvalidImageFormatException;

    /**
     * Decodes a message from a stego image using LSB steganography.
     * Metadata must be extracted beforehand to determine the LSB depth.
     *
     * @param stegoImageBytes The byte array representing the stego image containing the encoded message.
     * @param lsbDepth        The number of least significant bits per channel used for encoding.
     * @return A byte array representing the decoded message.
     * @throws InvalidLsbDepthException    If the specified LSB depth is invalid.
     * @throws LsbDecodingException        If an error occurs during the decoding process.
     * @throws StegoDataNotFoundException  If no stego data is found in the image.
     * @throws InvalidImageFormatException If the provided image format is invalid.
     */
    byte[] decodeMessage(
            byte[] stegoImageBytes,
            int lsbDepth
    ) throws InvalidLsbDepthException, LsbDecodingException, StegoDataNotFoundException, InvalidImageFormatException;

    /**
     * Encodes a file into an image using LSB steganography.
     *
     * @param imageBytes The byte array representing the image to encode the file into.
     * @param fileBytes  The byte array representing the file to encode.
     * @param metadata   The metadata containing encoding details such as LSB depth.
     * @return A byte array representing the stego image with the encoded file.
     * @throws InvalidLsbDepthException    If the specified LSB depth is invalid.
     * @throws MessageTooLargeException    If the file is too large to fit in the image.
     * @throws LsbEncodingException        If an error occurs during the encoding process.
     * @throws InvalidImageFormatException If the provided image format is invalid.
     */
    byte[] encodeFile(
            byte[] imageBytes,
            byte[] fileBytes,
            StegoMetadataDTO metadata
    ) throws InvalidLsbDepthException, MessageTooLargeException, LsbEncodingException, InvalidImageFormatException;

    /**
     * Decodes a file from a stego image using LSB steganography.
     *
     * @param stegoImageBytes The byte array representing the stego image containing the encoded file.
     * @param lsbDepth        The number of least significant bits per channel used for encoding.
     * @return A byte array representing the decoded file.
     * @throws InvalidLsbDepthException    If the specified LSB depth is invalid.
     * @throws LsbDecodingException        If an error occurs during the decoding process.
     * @throws StegoDataNotFoundException  If no stego data is found in the image.
     * @throws InvalidImageFormatException If the provided image format is invalid.
     */
    byte[] decodeFile(
            byte[] stegoImageBytes,
            int lsbDepth
    ) throws InvalidLsbDepthException, LsbDecodingException, StegoDataNotFoundException, InvalidImageFormatException;

    /**
     * Extracts metadata from a stego image without decoding payload data.
     *
     * @param stegoImageBytes The byte array representing the stego image.
     * @return A DTO containing the extracted metadata, such as encoding details.
     * @throws MetadataNotFoundException   If no metadata is found in the image.
     * @throws MetadataDecodingException   If an error occurs during metadata extraction.
     * @throws InvalidImageFormatException If the provided image format is invalid.
     */
    StegoMetadataDTO extractMetadata(
            byte[] stegoImageBytes
    ) throws MetadataNotFoundException, MetadataDecodingException, InvalidImageFormatException;

}