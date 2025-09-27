package com.example.springprojectsteganographytool.models.capacity;

public record EstimationResult(
        long capacityBytes,
        long overheadBytes,
        long encryptedBytes,
        long requiredBytes,
        boolean fits
) {
}
