package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.models.capacity.EstimationResult;
import com.example.springprojectsteganographytool.services.CapacityUtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Utility functions for estimating steganographic capacity and required size
 * for a payload (plain + AES envelope) before doing expensive work.
 * <p>
 * Format costs (current version 1):
 * Header + metadata (LSB depth 1):
 * MAGIC(4) + VERSION(1) + META_LEN(4) + META_JSON(variable) + PAYLOAD_LEN(8)
 * <p>
 * Payload block (encoded at chosen LSB depth):
 * PAYLOAD bytes (ciphertext: salt + iv + padded cipher)
 */
@Service
@Slf4j
public class CapacityUtilServiceImpl implements CapacityUtilService {

    // Fixed overhead pieces (excluding META_JSON length)
    private static final int FIXED_HEADER_OVERHEAD = 4 + 1 + 4 + 8; // MAGIC + VERSION + META_LEN + PAYLOAD_LEN
    private static final int SALT_LEN = 16; // Length of the salt used in encryption (in bytes)
    private static final int IV_LEN = 16; // Length of the initialization vector (IV) used in encryption (in bytes)
    private static final int AES_BLOCK_SIZE = 16; // Block size for AES encryption (in bytes)

    /**
     * Computes the total capacity of an image in bytes based on its dimensions and LSB depth.
     *
     * @param width    The width of the image in pixels.
     * @param height   The height of the image in pixels.
     * @param lsbDepth The number of least significant bits used for encoding.
     * @return The total capacity of the image in bytes.
     */
    @Override
    public long computeTotalCapacityBytes(int width, int height, int lsbDepth) {
        log.debug("Computing capacity for image {}x{} at LSB depth {}", width, height, lsbDepth);
        // bits = pixels * 3 (RGB channels) * lsbDepth
        var bits = (long) width * height * 3 * lsbDepth;
        return bits / 8L;
    }

    /**
     * Estimates the steganographic capacity and requirements for embedding a payload.
     *
     * @param width              The width of the image in pixels.
     * @param height             The height of the image in pixels.
     * @param lsbDepth           The number of least significant bits used for encoding.
     * @param metadataJsonLength The length of the metadata JSON in bytes.
     * @param plainLength        The length of the plaintext payload in bytes.
     * @return An `EstimationResult` object containing the capacity, overhead, encrypted length,
     * total required size, and whether the payload fits within the capacity.
     */
    @Override
    public EstimationResult estimate(int width, int height, int lsbDepth, int metadataJsonLength, long plainLength) {

        log.debug("Estimating capacity for image {}x{} at LSB depth {}, metadata length {}, plain length {}",
                width, height, lsbDepth, metadataJsonLength, plainLength);

        var capacity = computeTotalCapacityBytes(width, height, lsbDepth); // Total capacity of the image
        var overhead = computeOverheadBytes(metadataJsonLength); // Overhead size in bytes
        var encryptedLength = estimateEncryptedLength(plainLength); // Encrypted payload size in bytes
        var required = overhead + encryptedLength; // Total required size in bytes

        return new EstimationResult(
                capacity,
                overhead,
                encryptedLength,
                required,
                required <= capacity // Whether the payload fits within the capacity
        );
    }

    // ----- Helpers ----- //

    /**
     * Computes the overhead in bytes based on the length of the metadata JSON.
     *
     * @param metadataJsonLength The length of the metadata JSON in bytes.
     * @return The total overhead in bytes.
     */
    private long computeOverheadBytes(int metadataJsonLength) {
        log.debug("Computing overhead for metadata length {}", metadataJsonLength);
        return FIXED_HEADER_OVERHEAD + metadataJsonLength;
    }

    /**
     * Estimates the length of the encrypted payload in bytes, including padding, salt, and IV.
     *
     * @param plainLength The length of the plaintext payload in bytes.
     * @return The total length of the encrypted payload in bytes.
     */
    private long estimateEncryptedLength(long plainLength) {
        log.debug("Estimating encrypted length for plain length {}", plainLength);
        // Calculate padding to align with AES block size
        var padding = AES_BLOCK_SIZE - (plainLength % AES_BLOCK_SIZE);

        if (padding == 0) {
            padding = AES_BLOCK_SIZE; // Full block padding if no remainder
        }

        // Total encrypted length = salt + IV + plaintext + padding
        return SALT_LEN + IV_LEN + plainLength + padding;
    }
}
