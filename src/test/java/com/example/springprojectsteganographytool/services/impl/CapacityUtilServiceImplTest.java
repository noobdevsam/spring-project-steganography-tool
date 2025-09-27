package com.example.springprojectsteganographytool.services.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CapacityUtilServiceImplTest {

    private final CapacityUtilServiceImpl capacityUtil = new CapacityUtilServiceImpl();

    @Test
    void testBasicEstimationFits() {
        int width = 100;
        int height = 100;
        int lsbDepth = 1;
        long plainLen = 100; // bytes
        int metaLen = 110;

        var result = capacityUtil.estimate(width, height, lsbDepth, metaLen, plainLen);

        // capacity = 100*100*3 bits /8 = 30000/8 = 3750 bytes
        assertEquals(3750, result.capacityBytes());
        assertTrue(result.overheadBytes() > metaLen); // overhead includes fixed header
        assertTrue(result.encryptedBytes() > plainLen); // ciphertext bigger than plain
        assertTrue(result.requiredBytes() > plainLen);
        assertTrue(result.fits());
    }

    @Test
    void testTooLarge() {
        int width = 50;
        int height = 50;
        int lsbDepth = 1;
        long plainLen = 10_000;
        int metaLen = 120;

        var result = capacityUtil.estimate(width, height, lsbDepth, metaLen, plainLen);
        assertFalse(result.fits());
    }
}