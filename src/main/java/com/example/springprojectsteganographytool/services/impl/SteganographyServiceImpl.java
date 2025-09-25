package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.entities.StegoData;
import com.example.springprojectsteganographytool.exceptions.data.MessageTooLargeException;
import com.example.springprojectsteganographytool.exceptions.data.StegoDataNotFoundException;
import com.example.springprojectsteganographytool.exceptions.data.StorageException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesKeyInvalidException;
import com.example.springprojectsteganographytool.exceptions.encryption.AesOperationException;
import com.example.springprojectsteganographytool.exceptions.encryption.InvalidEncryptionKeyException;
import com.example.springprojectsteganographytool.exceptions.file.FileTooLargeException;
import com.example.springprojectsteganographytool.exceptions.lsb.InvalidLsbDepthException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbDecodingException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbEncodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataDecodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataEncodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataNotFoundException;
import com.example.springprojectsteganographytool.mappers.StegoDataMapper;
import com.example.springprojectsteganographytool.models.StegoDecodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoDownloadDTO;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoMetadataDTO;
import com.example.springprojectsteganographytool.repos.StegoDataRepository;
import com.example.springprojectsteganographytool.services.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class SteganographyServiceImpl implements SteganographyService {

    private final AesUtilService aesUtilService;
    private final LsbUtilService lsbUtilService;
    private final CapacityUtilService capacityUtilService;
    private final StegoDataRepository stegoDataRepository;
    private final StegoDataMapper stegoDataMapper;
    private final StorageService storageService;
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;

    public SteganographyServiceImpl(
            AesUtilService aesUtilService,
            LsbUtilService lsbUtilService,
            CapacityUtilService capacityUtilService,
            StegoDataRepository stegoDataRepository,
            StegoDataMapper stegoDataMapper,
            StorageService storageService,
            ExecutorService executorService,
            ObjectMapper objectMapper) {
        this.aesUtilService = aesUtilService;
        this.lsbUtilService = lsbUtilService;
        this.capacityUtilService = capacityUtilService;
        this.stegoDataRepository = stegoDataRepository;
        this.stegoDataMapper = stegoDataMapper;
        this.storageService = storageService;
        this.executorService = executorService;
        this.objectMapper = objectMapper;
    }

    @Transactional(
            rollbackFor = {
                    InvalidLsbDepthException.class,
                    MessageTooLargeException.class,
                    InvalidEncryptionKeyException.class,
                    LsbEncodingException.class,
                    AesOperationException.class,
                    MetadataEncodingException.class,
                    StorageException.class,
                    ExecutionException.class,
                    InterruptedException.class
            },
            propagation = Propagation.REQUIRED
    )
    @Override
    public StegoEncodeResponseDTO encodeText(BufferedImage coverImage, String message, String password, int lsbDepth) throws InvalidLsbDepthException, MessageTooLargeException, InvalidEncryptionKeyException, LsbEncodingException, AesOperationException, MetadataEncodingException, StorageException, ExecutionException, InterruptedException {
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

            var encodedBytes = executorService.submit(
                    () -> aesUtilService.encryptText(message, password)
            ).get();

            var coverBytes = bufferedImageToPngBytes(coverImage);
            var stegoBytes = executorService.submit(
                    () -> lsbUtilService.encode(coverBytes, encodedBytes, metadata)
            ).get();

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
            switch (e) {
                case InvalidLsbDepthException invalidLsbDepthException -> throw invalidLsbDepthException;
                case MessageTooLargeException messageTooLargeException -> throw messageTooLargeException;
                case InvalidEncryptionKeyException invalidEncryptionKeyException -> throw invalidEncryptionKeyException;
                case LsbEncodingException lsbEncodingException -> throw lsbEncodingException;
                case AesOperationException aesOperationException -> throw aesOperationException;
                case MetadataEncodingException metadataEncodingException -> throw metadataEncodingException;
                case StorageException storageException -> throw storageException;
                case ExecutionException executionException -> throw executionException;
                case InterruptedException interruptedException -> throw interruptedException;
                default -> throw new StorageException(e.getMessage(), e.getCause());

            }
        }

    }

    @Override
    public StegoEncodeResponseDTO encodeFile(BufferedImage coverImage, String originalFileName, byte[] fileBytes, String password, int lsbDepth) throws InvalidLsbDepthException, FileTooLargeException, InvalidEncryptionKeyException, LsbEncodingException, AesOperationException, MetadataEncodingException, StorageException, ExecutionException, InterruptedException {
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

            var encodedBytes = executorService.submit(
                    () -> aesUtilService.encryptFile(fileBytes, password)
            ).get();

            var coverBytes = bufferedImageToPngBytes(coverImage);
            var stegoBytes = executorService.submit(
                    () -> lsbUtilService.encode(coverBytes, encodedBytes, metadata)
            ).get();

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
            switch (e) {
                case InvalidLsbDepthException invalidLsbDepthException -> throw invalidLsbDepthException;
                case MessageTooLargeException messageTooLargeException -> throw messageTooLargeException;
                case InvalidEncryptionKeyException invalidEncryptionKeyException -> throw invalidEncryptionKeyException;
                case LsbEncodingException lsbEncodingException -> throw lsbEncodingException;
                case AesOperationException aesOperationException -> throw aesOperationException;
                case MetadataEncodingException metadataEncodingException -> throw metadataEncodingException;
                case StorageException storageException -> throw storageException;
                case ExecutionException executionException -> throw executionException;
                case InterruptedException interruptedException -> throw interruptedException;
                default -> throw new StorageException(e.getMessage(), e.getCause());
            }
        }

    }

    @Override
    public StegoDecodeResponseDTO decodeProcess(BufferedImage stegoImage, String password) throws InvalidEncryptionKeyException, MetadataNotFoundException, StegoDataNotFoundException, LsbDecodingException, AesOperationException, MetadataDecodingException, ExecutionException, InterruptedException {

        try {

            var stegoBytes = bufferedImageToPngBytes(stegoImage); // Convert BufferedImage to byte array in PNG format

            var metadata = executorService.submit(
                    () -> lsbUtilService.extractMetadata(stegoBytes)
            ).get(); // Extract metadata from the stego image bytes
            if (metadata == null) {
                throw new MetadataNotFoundException("No metadata found in the provided image.");
            }

            var providedKeyHash = aesUtilService.generateKey(password); // Generate the key hash from the provided password
            if (!providedKeyHash.equals(metadata.encryptionKeyHash())) {
                throw new AesKeyInvalidException("Provided password does not match the encryption key.");
            }

            if (metadata.hasText()) {
                var encodedText = executorService.submit(
                        () -> lsbUtilService.decode(stegoBytes, metadata.lsbDepth())
                ).get(); // Decode the text from the stego image bytes

                var text = executorService.submit(
                        () -> aesUtilService.decryptText(encodedText, password)
                ).get(); // Decrypt the encoded text using the provided password

                return new StegoDecodeResponseDTO(
                        text, null, null, true, false
                );
            } else if (metadata.hasFile()) {
                var encodedFile = executorService.submit(
                        () -> lsbUtilService.decode(stegoBytes, metadata.lsbDepth())
                ).get();

                var fileBytes = executorService.submit(
                        () -> aesUtilService.decryptFile(encodedFile, password)
                ).get(); // Decrypt the encoded file using the provided password

                return new StegoDecodeResponseDTO(
                        null, metadata.originalFileName(), fileBytes, false, true
                );
            } else {
                throw new MetadataDecodingException("No text or file data found in the provided image.");
            }

        } catch (Exception e) {
            switch (e) {
                case InvalidLsbDepthException invalidLsbDepthException -> throw invalidLsbDepthException;
                case MessageTooLargeException messageTooLargeException -> throw messageTooLargeException;
                case InvalidEncryptionKeyException invalidEncryptionKeyException -> throw invalidEncryptionKeyException;
                case LsbEncodingException lsbEncodingException -> throw lsbEncodingException;
                case AesOperationException aesOperationException -> throw aesOperationException;
                case MetadataEncodingException metadataEncodingException -> throw metadataEncodingException;
                case StorageException storageException -> throw storageException;
                case ExecutionException executionException -> throw executionException;
                case InterruptedException interruptedException -> throw interruptedException;
                default -> throw new StorageException(e.getMessage(), e.getCause());
            }
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

    @Override
    public void deleteById(UUID id) throws StegoDataNotFoundException {

        if (!stegoDataRepository.existsById(id)) {
            throw new StegoDataNotFoundException("Stego data with ID: " + id + " not found.");
        }

        stegoDataRepository.deleteById(id);

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
            BufferedImage coverImage, StegoMetadataDTO metadata, int plainPayloadLength
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

}
