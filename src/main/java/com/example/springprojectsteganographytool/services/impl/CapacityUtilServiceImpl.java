package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.models.capacity.EstimationResult;
import com.example.springprojectsteganographytool.services.CapacityUtilService;
import org.springframework.stereotype.Service;

@Service
public class CapacityUtilServiceImpl implements CapacityUtilService {

    // Fixed overhead pieces (excluding META_JSON length)
    private static final int FIXED_HEADER_OVERHEAD = 4 + 1 + 4 + 8; // MAGIC + VERSION + META_LEN + PAYLOAD_LEN
    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 16;
    private static final int AES_BLOCK_SIZE = 16;

    private CapacityUtilServiceImpl() {
    }

    @Override
    public long computeTotalCapacityBytes(int width, int height, int lsbDepth) {
        // bits = pixels * 3 * lsbDepth
        var bits = (long) width * height * 3 * lsbDepth;
        return bits / 8L;
    }

    @Override
    public long computeOverheadBytes(int metadataJsonLength) {
        return FIXED_HEADER_OVERHEAD + metadataJsonLength;
    }

    @Override
    public long estimateEncryptedLength(long plainLength) {
        var padding = AES_BLOCK_SIZE - (plainLength % AES_BLOCK_SIZE);

//        if (padding == 0) {
//            padding = AES_BLOCK_SIZE;
//        }
//
        return SALT_LEN + IV_LEN + plainLength + padding;
    }

    @Override
    public EstimationResult estimate(int width, int height, int lsbDepth, int metadataJsonLength, long plainLength) {
        var capacity = computeTotalCapacityBytes(width, height, lsbDepth);
        var overhead = computeOverheadBytes(metadataJsonLength);
        var encryptedLength = estimateEncryptedLength(plainLength);
        var required = overhead + encryptedLength;

        return new EstimationResult(
                capacity,
                overhead,
                encryptedLength,
                required,
                required <= capacity
        );
    }
}
