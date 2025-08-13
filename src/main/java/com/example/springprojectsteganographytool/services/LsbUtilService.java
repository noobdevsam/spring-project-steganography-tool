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


    byte[] encode(
            byte[] imageBytes,
            byte[] payloadBytes,
            StegoMetadataDTO metadata
    ) throws InvalidLsbDepthException, MessageTooLargeException, LsbEncodingException, InvalidImageFormatException;


    byte[] decode(
            byte[] stegoImageBytes,
            Integer lsbDepth
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