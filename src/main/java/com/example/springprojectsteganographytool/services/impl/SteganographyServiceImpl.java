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
import org.springframework.core.io.Resource;
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

    /**
     * Encodes a file stream into an image using steganography.
     *
     * @param coverImage        The cover image in which the file stream will be embedded.
     * @param coverImageName    The name of the cover image.
     * @param nameOfFileToEmbed The name of the file to embed.
     * @param fileStream        The input stream of the file to embed.
     * @param fileSize          The size of the file to embed.
     * @param password          The password used for encrypting the file.
     * @param lsbDepth          The LSB (Least Significant Bit) depth to use for encoding.
     * @return A `StegoEncodeResponseDTO` containing details about the encoded stego image.
     * @throws Exception If an error occurs during the encoding process.
     */
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

        // Validate the LSB depth to ensure it is either 1 or 2
        validateLsbDepth(lsbDepth);

        log.debug("Encoding file stream into image.");

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

        try {
            // Perform an early capacity check to ensure the file can fit in the image
            earlyCapacityCheck(coverImage, metadata, fileSize);
        } catch (MessageTooLargeException e) {
            throw new RuntimeException(e);
        }

        // Encrypt the file stream and save it to a temporary file
        var encTemp = largeFileEncryptionService.encryptToTempFile(fileStream, password);

        try {
            // Convert the cover image to a PNG byte array
            var coverBytes = bufferedImageToPngBytes(coverImage);

            // Get the length of the encrypted file
            var encryptedLength = encTemp.length();

            // Perform a precise capacity check to ensure the encrypted file can fit in the image
            preciseCapacityCheck(coverImage, metadata, encryptedLength);

            try (var encIn = Files.newInputStream(encTemp.path())) {
                // Encode the encrypted file stream into the cover image
                var stegoBytes = lsbUtilService.encodeStream(coverBytes, encIn, encryptedLength, metadata);

                // Generate a unique file name for the stego image
                var baseName = sanitizeBaseName(nameOfFileToEmbed);
                var fileName = ("stego-" + baseName + "-" + UUID.randomUUID() + ".png");

                // Save the stego image to storage
                storageService.save(fileName, stegoBytes);

                log.debug("File stream encoded and saved as stego file: {}", fileName);

                // Save the stego data to the repository and return the response DTO
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
            // Delete the temporary encrypted file
            try {
                Files.deleteIfExists(encTemp.path());
            } catch (Exception e) {
                log.warn("Failed to delete temp encrypted file {} : {}", encTemp.path(), e.getMessage());
            }
        }
    }

    /**
     * Decodes a stego image to extract either hidden text or a file.
     *
     * @param stegoImage The stego image containing the hidden data.
     * @param password   The password used to decrypt the hidden data.
     * @return A `StegoDecodeResponseDTO` containing the extracted data and metadata.
     * @throws Exception If an error occurs during the decoding process.
     */
    @Override
    public StegoDecodeResponseDTO decodeProcess(
            BufferedImage stegoImage,
            String password
    ) throws Exception {

        log.debug("Decoding stego image with provided password.");

        // Extract metadata directly from the BufferedImage (no intermediate PNG serialization)
        var metadata = lsbUtilService.extractMetadata(stegoImage);

        // Throw an exception if no metadata is found in the image
        if (metadata == null) {
            throw new MetadataNotFoundException("No metadata found in the provided image.");
        }

        // Generate a key hash from the provided password and validate it against the metadata
        var providedKeyHash = aesUtilService.generateKey(password);
        if (!providedKeyHash.equals(metadata.encryptionKeyHash())) {
            throw new AesKeyInvalidException("Provided password does not match the encryption key.");
        }

        // Check if the metadata indicates hidden text
        if (metadata.hasText()) {
            // Decode the hidden text from the stego image
            var encodedText = lsbUtilService.decode(
                    stegoImage, metadata.lsbDepth()
            );
            // Decrypt the extracted text using the provided password
            var text = aesUtilService.decryptText(encodedText, password);
            var now = System.currentTimeMillis();

            // Return the response DTO with the extracted text
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
            // Extract the hidden file bytes from the stego image
            var encodedFile = lsbUtilService.decode(stegoImage, metadata.lsbDepth());
            var fileBytes = aesUtilService.decryptFile(encodedFile, password);

            // Persist the extracted file temporarily
            var created = System.currentTimeMillis();
            var expires = created + extractionTempTtlMs;

            // Sanitize the base name of the file
            var baseName = sanitizeBaseName(metadata.nameOfFileToEmbed());
            var extension = "";

            // Preserve the original file extension if available
            if (metadata.nameOfFileToEmbed() != null) {
                var dot = metadata.nameOfFileToEmbed().lastIndexOf('.');
                if (dot > 0 && dot < metadata.nameOfFileToEmbed().length() - 1) {
                    extension = metadata.nameOfFileToEmbed().substring(dot);
                    extension = extension.replaceAll("[^A-Za-z0-9._-]", "");
                }
            }

            // Create a unique temporary file name for the extracted file
            var tempFileName = "extracted-" + created + "-" + UUID.randomUUID() + "-" + baseName + extension;
            var savedPath = storageService.save(tempFileName, fileBytes);
            log.debug("Extracted file saved to {} (expires at {})", savedPath, expires);

            log.debug("File extracted from stego image: {}, size: {}", tempFileName, fileBytes.length);

            // Return the response DTO with the extracted file details
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
            // Throw an exception if no text or file data is found in the image
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

    /**
     * Downloads a stego file as a Spring `Resource` object.
     * <p>
     * This method retrieves the stego data associated with the given ID from the repository.
     * If the data is found, it loads the corresponding file as a `Resource` object.
     * If the data is not found, a `StegoDataNotFoundException` is thrown.
     *
     * @param id The unique identifier of the stego file to download.
     * @return The stego file as a `Resource` object.
     * @throws StegoDataNotFoundException If no stego data is found for the given ID.
     */
    @Override
    public Resource downloadStegoFile(UUID id) throws StegoDataNotFoundException {
        log.debug("Downloading stego file by ID: {}", id);

        var stegoData = stegoDataRepository.findById(id)
                .orElseThrow(() -> new StegoDataNotFoundException("Stego data with ID: " + id + " not found."));

        return storageService.loadAsResource(stegoData.getStegoFileName());
    }

    /**
     * Downloads an extracted file as a Spring `Resource` object.
     * <p>
     * This method attempts to load the specified file from storage and return it as a `Resource`.
     * If the file cannot be loaded, a `StorageException` is logged, and `null` is returned.
     * </p>
     *
     * @param fileName The name of the file to be downloaded.
     * @return The extracted file as a `Resource` object, or `null` if the file cannot be loaded.
     * @throws StorageException If an error occurs while loading the file.
     */
    @Override
    public Resource downloadExtractedFile(String fileName) throws StorageException {
        log.debug("Downloading extracted file: {}", fileName);
        try {
            return storageService.loadAsResource(fileName);
        } catch (StorageException e) {
            log.error("Failed to load extracted file {}: {}", fileName, e.getMessage());
            return null;
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
