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
import com.example.springprojectsteganographytool.models.StegoDownloadDTO;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SteganographyServiceImpl implements SteganographyService {

    private final AesUtilService aesUtilService;
    private final LsbUtilService lsbUtilService;
    private final CapacityUtilService capacityUtilService;
    private final StegoDataRepository stegoDataRepository;
    private final StegoDataMapper stegoDataMapper;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    private final LargeFileEncryptionService largeFileEncryptionService;
    private final long streamThreshold;

    public SteganographyServiceImpl(
            AesUtilService aesUtilService,
            LsbUtilService lsbUtilService,
            CapacityUtilService capacityUtilService,
            StegoDataRepository stegoDataRepository,
            StegoDataMapper stegoDataMapper,
            StorageService storageService,
            ObjectMapper objectMapper,
            LargeFileEncryptionService largeFileEncryptionService,
            @Value("${app.stream.threshold-bytes:5242880}") long streamThreshold
    ) {
        this.aesUtilService = aesUtilService;
        this.lsbUtilService = lsbUtilService;
        this.capacityUtilService = capacityUtilService;
        this.stegoDataRepository = stegoDataRepository;
        this.stegoDataMapper = stegoDataMapper;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
        this.largeFileEncryptionService = largeFileEncryptionService;
        this.streamThreshold = streamThreshold;
    }

    @Transactional(
            rollbackFor = {Exception.class},
            propagation = Propagation.REQUIRED
    )
    @Override
    public StegoEncodeResponseDTO encodeText(BufferedImage coverImage, String message, String password, int lsbDepth) throws Exception {
        validateLsbDepth(lsbDepth);

        try {
            var keyHash = aesUtilService.generateKey(password);
            var metadata = new StegoMetadataDTO(
                    lsbDepth,
                    true,
                    false,
                    keyHash,
                    null
            );

            // Phase 1: Early capacity estimation before encryption
            earlyCapacityCheck(coverImage, metadata, message.getBytes().length);

            var encodedBytes = aesUtilService.encryptText(message, password);

            var coverBytes = bufferedImageToPngBytes(coverImage);
            var stegoBytes = lsbUtilService.encode(coverBytes, encodedBytes, metadata);

            var safeName = "stego-text-" + UUID.randomUUID() + ".png";
            var _ = storageService.save(safeName, stegoBytes);

            var savedData = stegoDataRepository.save(
                    StegoData.builder()
                            .originalFileName(null)
                            .embeddedFileName(null)
                            .stegoFileName(safeName)
                            .stegoFileSize((long) stegoBytes.length)
                            .encryptionKeyHash(keyHash)
                            .hasText(true)
                            .hasFile(false)
                            .build()
            );

            return stegoDataMapper.StegoDataToEncodeResponseDTO(savedData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Transactional(
            rollbackFor = {Exception.class},
            propagation = Propagation.REQUIRED
    )
    @Override
    public StegoEncodeResponseDTO encodeFile(BufferedImage coverImage, String originalFileName, byte[] fileBytes, String password, int lsbDepth) throws Exception {
        validateLsbDepth(lsbDepth);

        try {
            var keyHash = aesUtilService.generateKey(password);
            var metadata = new StegoMetadataDTO(
                    lsbDepth,
                    false,
                    true,
                    keyHash,
                    originalFileName
            );

            // Phase 1: Early capacity estimation before encryption
            earlyCapacityCheck(coverImage, metadata, fileBytes.length);

            var encodedBytes = aesUtilService.encryptFile(fileBytes, password);

            var coverBytes = bufferedImageToPngBytes(coverImage);
            var stegoBytes = lsbUtilService.encode(coverBytes, encodedBytes, metadata);

            // save the stego image to storage
            var baseName = originalFileName != null ? originalFileName : "embedded-file";
            var dot = baseName.lastIndexOf('.');
            if (dot > 0) {
                baseName = baseName.substring(0, dot);
            }
            var safeName = ("stego-" + baseName + "-" + UUID.randomUUID() + ".png").replaceAll("[^A-Za-z0-9._-]", "_");
            var _ = storageService.save(safeName, stegoBytes);

            var savedData = stegoDataRepository.save(
                    StegoData.builder()
                            .originalFileName(null)
                            .embeddedFileName(originalFileName)
                            .stegoFileName(safeName)
                            .stegoFileSize((long) stegoBytes.length)
                            .encryptionKeyHash(keyHash)
                            .hasText(false)
                            .hasFile(true)
                            .build()
            );

            return stegoDataMapper.StegoDataToEncodeResponseDTO(savedData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Transactional(
            rollbackFor = {Exception.class},
            propagation = Propagation.REQUIRED
    )
    @Override
    public StegoEncodeResponseDTO encodeFileStream(
            BufferedImage coverImage,
            String originalFileName,
            InputStream fileStream,
            long fileSize,
            String password,
            int lsbDepth
    ) throws Exception {

        validateLsbDepth(lsbDepth);

        var keyHash = aesUtilService.generateKey(password);
        var metadata = new StegoMetadataDTO(
                lsbDepth,
                false,
                true,
                keyHash,
                originalFileName
        );

        // Early capacity estimation before encryption
        earlyCapacityCheck(coverImage, metadata, fileSize);

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
                var baseName = sanitizeBaseName(originalFileName);
                var fileName = ("stego-" + baseName + "-" + UUID.randomUUID() + ".png");
                storageService.save(fileName, stegoBytes);

                return stegoDataMapper.StegoDataToEncodeResponseDTO(
                        stegoDataRepository.save(
                                StegoData.builder()
                                        .embeddedFileName(originalFileName)
                                        .stegoFileName(fileName)
                                        .stegoFileSize((long) stegoBytes.length)
                                        .encryptionKeyHash(keyHash)
                                        .hasText(false)
                                        .hasFile(true)
                                        .build()
                        )
                );
            }
        } finally {
            try {
                Files.deleteIfExists(encTemp.path());
            } catch (Exception e) {
                log.warn("Failed to delete temp encrypted file {} : {}", encTemp.path(), e.getMessage());
            }
        }

    }

    @Override
    public StegoDecodeResponseDTO decodeProcess(BufferedImage stegoImage, String password) throws Exception {

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

            return new StegoDecodeResponseDTO(
                    text, null, null, true, false
            );
        } else if (metadata.hasFile()) {
            var encodedFile = lsbUtilService.decode(stegoImage, metadata.lsbDepth());
            var fileBytes = aesUtilService.decryptFile(encodedFile, password);

            return new StegoDecodeResponseDTO(
                    null, metadata.originalFileName(), fileBytes, false, true
            );
        } else {
            throw new MetadataDecodingException("No text or file data found in the provided image.");
        }

    }


    // ----- Read operations -----

    @Override
    public List<StegoEncodeResponseDTO> listAllEncodings() {
        return stegoDataRepository.findAll()
                .stream()
                .map(stegoDataMapper::StegoDataToEncodeResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StegoEncodeResponseDTO getById(UUID id) throws StegoDataNotFoundException {
        var stegoData = stegoDataRepository.findById(id)
                .orElseThrow(() -> new StegoDataNotFoundException("Stego data with ID: " + id + " not found."));

        return stegoDataMapper.StegoDataToEncodeResponseDTO(stegoData);
    }

    @Transactional
    @Override
    public void deleteById(UUID id) throws StegoDataNotFoundException {

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

    @Override
    public StegoDownloadDTO downloadStegoImage(UUID id) throws StegoDataNotFoundException {
        var stegoData = stegoDataRepository.findById(id)
                .orElseThrow(() -> new StegoDataNotFoundException("Stego data with ID: " + id + " not found."));

        var fileName = stegoData.getStegoFileName();

        try {
            var path = storageService.resolve(fileName);
            var bytes = Files.readAllBytes(path);
            return new StegoDownloadDTO(fileName, "image/png", bytes);
        } catch (Exception e) {
            throw new StorageException("Failed to download stego image: " + e.getMessage(), e.getCause());
        }
    }

    // --- helpers ---

    private static void validateLsbDepth(int lsbDepth) throws InvalidLsbDepthException {
        if (lsbDepth != 1 && lsbDepth != 2) {
            throw new InvalidLsbDepthException("LSB depth must be 1 or 2.");
        }
    }

    private static byte[] bufferedImageToPngBytes(BufferedImage bufferedImage) {

        try (var baos = new ByteArrayOutputStream()) {
            // Always write PNG to preserve RGB 8-bit without loss
            var ok = ImageIO.write(bufferedImage, "png", baos);

            if (!ok) {
                throw new StorageException("Failed to write BufferedImage to PNG format.");
            }

            return baos.toByteArray();
        } catch (Exception e) {
            throw new StorageException("Error while converting image to PNG.", e);
        }
    }

    // Perform early capacity estimation (Phase 1)
    // Throw MessageTooLargeException if estimated required
    // bytes exceed image capacity
    private void earlyCapacityCheck(
            BufferedImage coverImage, StegoMetadataDTO metadata, long plainPayloadLength
    ) throws MessageTooLargeException {
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

    private void preciseCapacityCheck(BufferedImage coverImage, StegoMetadataDTO metadata, long encryptedLength) {
    }

    private Object sanitizeBaseName(String originalFileName) {
    }

}
