package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.entities.StegoData;
import com.example.springprojectsteganographytool.exceptions.data.MessageTooLargeException;
import com.example.springprojectsteganographytool.exceptions.data.StegoDataNotFoundException;
import com.example.springprojectsteganographytool.exceptions.data.StorageException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.lsb.InvalidLsbDepthException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataDecodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataNotFoundException;
import com.example.springprojectsteganographytool.mappers.StegoDataMapper;
import com.example.springprojectsteganographytool.models.StegoDecodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoMetadataDTO;
import com.example.springprojectsteganographytool.repos.StegoDataRepository;
import com.example.springprojectsteganographytool.services.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SteganographyServiceImpl implements SteganographyService {

    // Dependencies and services used by the SteganographyServiceImpl class
    private final AesUtilService aesUtilService; // Service for AES encryption and decryption
    private final LsbUtilService lsbUtilService; // Service for LSB encoding and decoding
    private final CapacityUtilService capacityUtilService; // Service for capacity estimation
    private final StegoDataRepository stegoDataRepository; // Repository for managing stego data entities
    private final StegoDataMapper stegoDataMapper; // Mapper for converting between entities and DTOs
    private final StorageService storageService; // Service for file storage operations
    private final ObjectMapper objectMapper; // ObjectMapper for JSON serialization and deserialization
    private final LargeFileEncryptionService largeFileEncryptionService; // Service for encrypting large files

    // Configuration property for temporary file time-to-live in milliseconds
    @Value("${app.extraction.temp-ttl-ms}")
    private long extractionTempTtlMs;

    /**
     * Constructor for SteganographyServiceImpl.
     *
     * @param aesUtilService             AES utility service for encryption and decryption.
     * @param lsbUtilService             LSB utility service for encoding and decoding.
     * @param capacityUtilService        Utility service for capacity estimation.
     * @param stegoDataRepository        Repository for managing stego data.
     * @param stegoDataMapper            Mapper for converting between entities and DTOs.
     * @param storageService             Service for file storage operations.
     * @param objectMapper               ObjectMapper for JSON serialization and deserialization.
     * @param largeFileEncryptionService Service for encrypting large files.
     */
    public SteganographyServiceImpl(
            AesUtilService aesUtilService,
            LsbUtilService lsbUtilService,
            CapacityUtilService capacityUtilService,
            StegoDataRepository stegoDataRepository,
            StegoDataMapper stegoDataMapper,
            StorageService storageService,
            ObjectMapper objectMapper,
            LargeFileEncryptionService largeFileEncryptionService
    ) {
        this.aesUtilService = aesUtilService;
        this.lsbUtilService = lsbUtilService;
        this.capacityUtilService = capacityUtilService;
        this.stegoDataRepository = stegoDataRepository;
        this.stegoDataMapper = stegoDataMapper;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
        this.largeFileEncryptionService = largeFileEncryptionService;
    }

    /**
     * Validates the LSB depth value.
     *
     * @param lsbDepth The LSB depth to validate.
     * @throws InvalidLsbDepthException If the LSB depth is not 1 or 2.
     */
    private static void validateLsbDepth(int lsbDepth) throws InvalidLsbDepthException {

        log.debug("Validating LSB depth: {}", lsbDepth);

        if (lsbDepth != 1 && lsbDepth != 2) {
            throw new InvalidLsbDepthException("LSB depth must be 1 or 2.");
        }
    }

    /**
     * Converts a BufferedImage to a PNG byte array.
     *
     * @param bufferedImage The BufferedImage to convert.
     * @return The PNG byte array.
     * @throws StorageException If an error occurs during conversion.
     */
    private static byte[] bufferedImageToPngBytes(BufferedImage bufferedImage) throws StorageException {

        log.debug("Converting BufferedImage to PNG byte array.");

        try (var outputStream = new ByteArrayOutputStream()) {
            // Always write PNG to preserve RGB 8-bit without loss
            var ok = ImageIO.write(bufferedImage, "png", outputStream);

            if (!ok) {
                throw new StorageException("Failed to write BufferedImage to PNG format.");
            }

            return outputStream.toByteArray();
        } catch (StorageException | IOException e) {
            throw new StorageException("Error while converting image to PNG:" + e.getMessage(), e);
        }
    }

    /**
     * Encodes a text message into an image using steganography.
     *
     * @param coverImage     The cover image in which the text message will be embedded.
     * @param coverImageName The name of the cover image.
     * @param message        The text message to encode.
     * @param password       The password used for encrypting the text message.
     * @param lsbDepth       The LSB (Least Significant Bit) depth to use for encoding.
     * @return A `StegoEncodeResponseDTO` containing details about the encoded stego image.
     * @throws RuntimeException If an unexpected error occurs during the encoding process.
     * @throws StorageException If an error occurs while saving the stego image.
     */
    @Transactional(
            rollbackFor = {Exception.class},
            propagation = Propagation.REQUIRED
    )
    @Override
    public StegoEncodeResponseDTO encodeText(
            BufferedImage coverImage,
            String coverImageName,
            String message,
            String password,
            int lsbDepth
    ) {
        // Validate the LSB depth to ensure it is either 1 or 2
        validateLsbDepth(lsbDepth);

        try {
            log.debug("Encoding text into image. Cover image: {}, Message length: {}, LSB depth: {}", coverImageName, message.length(), lsbDepth);

            var keyHash = aesUtilService.generateKey(password);

            // Create metadata for the stego image
            var metadata = new StegoMetadataDTO(
                    lsbDepth,
                    true,
                    false,
                    keyHash,
                    null
            );

            // Perform an early capacity check to ensure the message can fit in the image
            earlyCapacityCheck(coverImage, metadata, message.getBytes().length);

            // Encrypt the text message using the provided password
            var encodedBytes = aesUtilService.encryptText(message, password);

            // Convert the cover image to a PNG byte array
            var coverBytes = bufferedImageToPngBytes(coverImage);

            // Encode the encrypted message into the cover image
            var stegoBytes = lsbUtilService.encode(coverBytes, encodedBytes, metadata);

            // Generate a unique file name for the stego image
            var stegoFileName = "stego-text-" + UUID.randomUUID() + ".png";

            // Save the stego image to storage
            storageService.save(stegoFileName, stegoBytes);

            // Save the stego data to the repository
            var savedData = stegoDataRepository.save(
                    StegoData.builder()
                            .coverImageName(coverImageName)
                            .fileNameOfEmbeddedData(null)
                            .stegoFileName(stegoFileName)
                            .stegoFileSize((long) stegoBytes.length)
                            .hasText(true)
                            .hasFile(false)
                            .build()
            );

            log.debug("Text encoded and saved as stego file: {}", stegoFileName);

            // Return the response DTO with details about the encoded stego image
            return stegoDataMapper.stegoDataToEncodeResponseDTO(savedData);
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected failure during text encoding: " + e.getMessage(), e);
        }
    }

    /**
     * Encodes a file into an image using steganography.
     *
     * @param coverImage        The cover image in which the file will be embedded.
     * @param coverImageName    The name of the cover image.
     * @param nameOfFileToEmbed The name of the file to embed.
     * @param fileBytes         The byte array of the file to embed.
     * @param password          The password used for encrypting the file.
     * @param lsbDepth          The LSB (Least Significant Bit) depth to use for encoding.
     * @return A `StegoEncodeResponseDTO` containing details about the encoded stego image.
     * @throws RuntimeException If an unexpected error occurs during the encoding process.
     * @throws StorageException If an error occurs while saving the stego image.
     */
    @Transactional(
            rollbackFor = {Exception.class},
            propagation = Propagation.REQUIRED
    )
    @Override
    public StegoEncodeResponseDTO encodeFile(
            BufferedImage coverImage,
            String coverImageName,
            String nameOfFileToEmbed,
            byte[] fileBytes,
            String password,
            int lsbDepth
    ) {
        // Validate the LSB depth to ensure it is either 1 or 2
        validateLsbDepth(lsbDepth);

        try {

            log.debug("Encoding file into image. Cover image: {}, File name: {}, File size: {}, LSB depth: {}",
                    coverImageName, nameOfFileToEmbed, fileBytes.length, lsbDepth);

            // Generate a key hash from the provided password
            var keyHash = aesUtilService.generateKey(password);

            // Create metadata for the stego image
            var metadata = new StegoMetadataDTO(
                    lsbDepth,
                    false,
                    true,
                    keyHash,
                    nameOfFileToEmbed
            );

            // Perform an early capacity check to ensure the file can fit in the image
            earlyCapacityCheck(coverImage, metadata, fileBytes.length);

            // Encrypt the file using the provided password
            var encodedBytes = aesUtilService.encryptFile(fileBytes, password);

            // Convert the cover image to a PNG byte array
            var coverBytes = bufferedImageToPngBytes(coverImage);

            // Encode the encrypted file into the cover image
            var stegoBytes = lsbUtilService.encode(coverBytes, encodedBytes, metadata);

            // Generate a unique file name for the stego image
            var baseName = nameOfFileToEmbed != null ? nameOfFileToEmbed : "embedded-file";
            var dot = baseName.lastIndexOf('.');
            if (dot > 0) {
                baseName = baseName.substring(0, dot);
            }
            var safeName = ("stego-" + baseName + "-" + UUID.randomUUID() + ".png").replaceAll("[^A-Za-z0-9._-]", "_");

            // Save the stego image to storage
            storageService.save(safeName, stegoBytes);

            log.debug("File encoded and saved as stego file: {}", safeName);

            // Save the stego data to the repository and return the response DTO
            return stegoDataMapper.stegoDataToEncodeResponseDTO(
                    stegoDataRepository.save(
                            StegoData.builder()
                                    .coverImageName(coverImageName)
                                    .fileNameOfEmbeddedData(nameOfFileToEmbed)
                                    .stegoFileName(safeName)
                                    .stegoFileSize((long) stegoBytes.length)
                                    .hasText(false)
                                    .hasFile(true)
                                    .build()
                    )
            );
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected failure during file encoding: " + e.getMessage(), e);
        }
    }

    @Transactional(
            rollbackFor = {Exception.class},
            propagation = Propagation.REQUIRED
    )
    @Override
    public StegoEncodeResponseDTO encodeFileStream(
            BufferedImage coverImage,
            String coverImageName,
            String nameOfFileToEmbed,
            InputStream fileStream,
            long fileSize,
            String password,
            int lsbDepth
    ) throws Exception {

        validateLsbDepth(lsbDepth);

        log.debug("Encoding file stream into image.");

        var keyHash = aesUtilService.generateKey(password);
        var metadata = new StegoMetadataDTO(
                lsbDepth,
                false,
                true,
                keyHash,
                nameOfFileToEmbed
        );

        try {
            // Early capacity estimation before encryption
            earlyCapacityCheck(coverImage, metadata, fileSize);
        } catch (MessageTooLargeException e) {
            throw new RuntimeException(e);
        }

        // Streaming encryption -> temp file
        var encTemp = largeFileEncryptionService.encryptToTempFile(fileStream, password);

        try {
            // Strict precise capacity check now that we know the exact encrypted size
            var coverBytes = bufferedImageToPngBytes(coverImage);

            var encryptedLength = encTemp.length();

            // Validate capacity precisely (metadata + encrypted file size)
            preciseCapacityCheck(coverImage, metadata, encryptedLength);

            try (var encIn = Files.newInputStream(encTemp.path())) {
                var stegoBytes = lsbUtilService.encodeStream(coverBytes, encIn, encryptedLength, metadata);
                var baseName = sanitizeBaseName(nameOfFileToEmbed);
                var fileName = ("stego-" + baseName + "-" + UUID.randomUUID() + ".png");
                storageService.save(fileName, stegoBytes);

                log.debug("File stream encoded and saved as stego file: {}", fileName);

                return stegoDataMapper.stegoDataToEncodeResponseDTO(
                        stegoDataRepository.save(
                                StegoData.builder()
                                        .coverImageName(coverImageName)
                                        .fileNameOfEmbeddedData(nameOfFileToEmbed)
                                        .stegoFileName(fileName)
                                        .stegoFileSize((long) stegoBytes.length)
                                        .hasText(false)
                                        .hasFile(true)
                                        .build()
                        )
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                Files.deleteIfExists(encTemp.path());
            } catch (Exception e) {
                log.warn("Failed to delete temp encrypted file {} : {}", encTemp.path(), e.getMessage());
            }
        }

    }

    @Override
    public StegoDecodeResponseDTO decodeProcess(
            BufferedImage stegoImage,
            String password
    ) throws Exception {

        log.debug("Decoding stego image with provided password.");

        // Use direct BufferedImage metadata extraction (no intermediate PNG serialization)
        var metadata = lsbUtilService.extractMetadata(stegoImage);

        if (metadata == null) {
            throw new MetadataNotFoundException("No metadata found in the provided image.");
        }

        var providedKeyHash = aesUtilService.generateKey(password); // Generate the key hash from the provided password
        if (!providedKeyHash.equals(metadata.encryptionKeyHash())) {
            throw new AesKeyInvalidException("Provided password does not match the encryption key.");
        }

        if (metadata.hasText()) {
            var encodedText = lsbUtilService.decode(
                    stegoImage, metadata.lsbDepth()
            );
            var text = aesUtilService.decryptText(encodedText, password);
            var now = System.currentTimeMillis();

            return new StegoDecodeResponseDTO(
                    true,
                    false,
                    text,
                    null,
                    null,
                    null,
                    now,
                    now
            );
        } else if (metadata.hasFile()) {
            // Extract file bytes
            var encodedFile = lsbUtilService.decode(stegoImage, metadata.lsbDepth());
            var fileBytes = aesUtilService.decryptFile(encodedFile, password);

            // Persist extracted file temporarily
            var created = System.currentTimeMillis();
            var expires = created + extractionTempTtlMs;

            // Sanitize base name
            var baseName = sanitizeBaseName(metadata.nameOfFileToEmbed());
            var extension = "";

            // Try to preserve original file extension if any
            if (metadata.nameOfFileToEmbed() != null) {
                var dot = metadata.nameOfFileToEmbed().lastIndexOf('.');
                if (dot > 0 && dot < metadata.nameOfFileToEmbed().length() - 1) {
                    extension = metadata.nameOfFileToEmbed().substring(dot);
                    extension = extension.replaceAll("[^A-Za-z0-9._-]", "");
                }
            }

            // Create a unique temp file name
            var tempFileName = "extracted-" + created + "-" + UUID.randomUUID() + "-" + baseName + extension;
            var savedPath = storageService.save(tempFileName, fileBytes);
            log.debug("Extracted file saved to {} (expires at {})", savedPath, expires);

            log.debug("File extracted from stego image: {}, size: {}", tempFileName, fileBytes.length);

            return new StegoDecodeResponseDTO(
                    false,
                    true,
                    null,
                    tempFileName,
                    (long) fileBytes.length,
                    savedPath.toAbsolutePath().toString(),
                    created,
                    expires
            );
        } else {
            throw new MetadataDecodingException("No text or file data found in the provided image.");
        }

    }

    @Override
    public List<StegoEncodeResponseDTO> listAllEncodings() {
        log.debug("Listing all stego encodings from the database.");
        return stegoDataRepository.findAll()
                .stream()
                .map(stegoDataMapper::stegoDataToEncodeResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StegoEncodeResponseDTO getById(UUID id) throws StegoDataNotFoundException {
        log.debug("Retrieving stego encoding by ID: {}", id);
        var stegoData = stegoDataRepository.findById(id)
                .orElseThrow(() -> new StegoDataNotFoundException("Stego data with ID: " + id + " not found."));

        return stegoDataMapper.stegoDataToEncodeResponseDTO(stegoData);
    }

    @Transactional
    @Override
    public void deleteById(UUID id) throws StegoDataNotFoundException {

        log.debug("Deleting stego encoding by ID: {}", id);

        var stegoData = stegoDataRepository.findById(id)
                .orElseThrow(() -> new StegoDataNotFoundException("Stego data with ID: " + id + " not found."));

        var fileName = stegoData.getStegoFileName();
        stegoDataRepository.delete(stegoData);
        log.debug("Deleted DB record for stego id={}  file={}", id, fileName);

        try {
            var deleted = storageService.delete(fileName);

            if (!deleted) {
                log.warn("Stego file {} not found during deletion cascade", fileName);
            }
        } catch (Exception e) {
            // Do not rollback DB delete for a file system issue, just log it
            log.warn("Failed to delete stego file {} after DB delete: {}", fileName, e.getMessage());
        }

    }

    // ----- Helpers -----

    /**
     * Performs an early capacity check to ensure that the payload can fit into the cover image
     * with the specified LSB depth and metadata.
     *
     * @param coverImage         The cover image to be used for encoding.
     * @param metadata           The metadata containing encoding details such as LSB depth.
     * @param plainPayloadLength The length of the plain (unencrypted) payload.
     * @throws MessageTooLargeException If the payload is too large to fit into the cover image.
     */
    private void earlyCapacityCheck(
            BufferedImage coverImage, StegoMetadataDTO metadata, long plainPayloadLength
    ) throws MessageTooLargeException {

        log.debug("Performing early capacity check. Image size: {}x{}, LSB depth: {}, Payload length: {}",
                coverImage.getWidth(), coverImage.getHeight(), metadata.lsbDepth(), plainPayloadLength);

        try {
            var metaJsonBytes = objectMapper.writeValueAsBytes(metadata);
            var estimation = capacityUtilService.estimate(
                    coverImage.getWidth(),
                    coverImage.getHeight(),
                    metadata.lsbDepth(),
                    metaJsonBytes.length,
                    plainPayloadLength
            );

            if (!estimation.fits()) {
                throw new MessageTooLargeException(
                        "Payload too large for image with LSB depth %d. Capacity=%d required≈%d (overhead=%d, encrypted≈%d)"
                                .formatted(
                                        metadata.lsbDepth(),
                                        estimation.capacityBytes(),
                                        estimation.requiredBytes(),
                                        estimation.overheadBytes(),
                                        estimation.encryptedBytes()
                                )
                );
            }
        } catch (MessageTooLargeException e) {
            throw e;
        } catch (Exception e) {
            // Fallback: do not hide unexpected errors
            throw new MessageTooLargeException(
                    "Failed early capacity check: " + e.getMessage(), e);
        }
    }

    /**
     * Performs a precise capacity check to ensure that the encrypted payload can fit into the
     * cover image with the specified LSB depth and metadata.
     *
     * @param coverImage      The cover image to be used for encoding.
     * @param metadata        The metadata containing encoding details such as LSB depth.
     * @param encryptedLength The length of the encrypted payload.
     * @throws MessageTooLargeException If the encrypted payload is too large to fit into the cover image.
     */
    private void preciseCapacityCheck(BufferedImage coverImage, StegoMetadataDTO metadata, long encryptedLength) {

        log.debug("Performing precise capacity check. Image size: {}x{}, LSB depth: {}, Encrypted payload length: {}",
                coverImage.getWidth(), coverImage.getHeight(), metadata.lsbDepth(), encryptedLength);

        try {
            var metaJsonBytes = objectMapper.writeValueAsBytes(metadata);
            long capacity = capacityUtilService.computeTotalCapacityBytes(
                    coverImage.getWidth(),
                    coverImage.getHeight(),
                    metadata.lsbDepth()
            );
            long overhead = (4 + 1 + 4 + metaJsonBytes.length + 8); // same format overhead
            long required = overhead + encryptedLength;

            if (required > capacity) {
                throw new MessageTooLargeException(
                        "Encrypted payload does not fit (post-encryption) capacity=%d required=%d overhead=%d"
                                .formatted(capacity, required, overhead)
                );
            }
        } catch (MessageTooLargeException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageTooLargeException("Precise capacity check failed: " + e.getMessage(), e);
        }
    }

    /**
     * Sanitizes the base name of a file by removing invalid characters and extracting the name
     * without the file extension.
     *
     * @param originalFileName The original file name to sanitize.
     * @return The sanitized base name of the file.
     */
    private String sanitizeBaseName(String originalFileName) {

        log.debug("Sanitizing base name from original file name: {}", originalFileName);

        if (originalFileName == null || originalFileName.isBlank()) {
            return "embedded-file";
        }
        var baseName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        var dot = baseName.lastIndexOf('.');

        if (dot > 0) {
            baseName = baseName.substring(0, dot);
        }
        return baseName;
    }

}
