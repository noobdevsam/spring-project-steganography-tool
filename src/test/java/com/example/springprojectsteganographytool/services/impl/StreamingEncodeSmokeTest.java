package com.example.springprojectsteganographytool.services.impl;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Disabled by default; enable when LargeFileEncryptionService is available in test context.
 */
//@Disabled
class StreamingEncodeSmokeTest {

    private final LargeFileEncryptionServiceImpl large = new LargeFileEncryptionServiceImpl();

    @Test
    void streamingEncryptionAndEncodeRoundTrip() throws Exception {
        var img = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
        var g = img.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 800, 800);

        byte[] bigPlain = new byte[6_000_000]; // 6MB
        for (int i = 0; i < bigPlain.length; i++) {
            bigPlain[i] = (byte) (i % 251); // Fill with some data
        }

        var encTemp = large.encryptToTempFile(new ByteArrayInputStream(bigPlain), "password");

        assertNotNull(encTemp);
    }
}