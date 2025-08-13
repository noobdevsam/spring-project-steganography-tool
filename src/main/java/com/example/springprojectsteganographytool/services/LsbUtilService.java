package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.exceptions.data.MessageTooLargeException;
import com.example.springprojectsteganographytool.exceptions.data.StegoDataNotFoundException;
import com.example.springprojectsteganographytool.exceptions.file.InvalidImageFormatException;
import com.example.springprojectsteganographytool.exceptions.lsb.InvalidLsbDepthException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbDecodingException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbEncodingException;
import com.example.springprojectsteganographytool.models.StegoMetadataDTO;

/**
 * Service interface for performing Least Significant Bit (LSB) steganography operations.
 * Provides methods for encoding data into images, decoding data from stego images,
 * and extracting metadata from stego images.
 */
public interface LsbUtilService {

    /**
     * Encodes a payload into an image using LSB steganography.
     *
     * @param imageBytes   The byte array representing the original image.
     * @param payloadBytes The byte array representing the payload to encode.
     * @param metadata     Metadata containing encoding details.
     * @return A byte array representing the stego image with the encoded payload.
     * @throws InvalidLsbDepthException    If the specified LSB depth is invalid.
     * @throws MessageTooLargeException    If the payload is too large to fit in the image.
     * @throws LsbEncodingException        If an error occurs during encoding.
     * @throws InvalidImageFormatException If the provided image format is invalid.
     */
    byte[] encode(
            byte[] imageBytes,
            byte[] payloadBytes,
            StegoMetadataDTO metadata
    ) throws InvalidLsbDepthException, MessageTooLargeException, LsbEncodingException, InvalidImageFormatException;

    /**
     * Decodes a payload from a stego image using LSB steganography.
     *
     * @param stegoImageBytes The byte array representing the stego image.
     * @param lsbDepth        The LSB depth used during encoding.
     * @return A byte array representing the decoded payload.
     * @throws InvalidLsbDepthException    If the specified LSB depth is invalid.
     * @throws LsbDecodingException        If an error occurs during decoding.
     * @throws StegoDataNotFoundException  If no stego data is found in the image.
     * @throws InvalidImageFormatException If the provided image format is invalid.
     */
    byte[] decode(
            byte[] stegoImageBytes,
            Integer lsbDepth
    ) throws InvalidLsbDepthException, LsbDecodingException, StegoDataNotFoundException, InvalidImageFormatException;

}