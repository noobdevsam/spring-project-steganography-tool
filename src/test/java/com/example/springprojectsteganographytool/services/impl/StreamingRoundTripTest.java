package com.example.springprojectsteganographytool.services.impl;

import com.example.springprojectsteganographytool.models.StegoMetadataDTO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("Enable when Spring context or manual wiring is acceptable")
class StreamingRoundTripTest {

    private final AesUtilServiceImpl aes = new AesUtilServiceImpl();
    private final LargeFileEncryptionServiceImpl large = new LargeFileEncryptionServiceImpl();
    private final LsbUtilServiceImpl lsb = new LsbUtilServiceImpl();

    private static byte[] bufferedImageToPngBytes(BufferedImage image) throws Exception {
        try (var baos = new java.io.ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        }
    }

    private static BufferedImage bytesToImage(byte[] imageBytes) throws Exception {
        try (var bais = new ByteArrayInputStream(imageBytes)) {
            var img = javax.imageio.ImageIO.read(bais);
            if (img == null) throw new IllegalStateException("Invalid image");
            return img;
        }
    }

    @Test
    void largeFileStreamingRoundTrip() throws Exception {
        // Cover image
        var img = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
        var g = img.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        // Large plaintext
        byte[] plain = new byte[1_000_000];
        for (int i = 0; i < plain.length; i++) plain[i] = (byte) (i % 251);

        var password = "strong-pass";
        // Streaming encryption
        var encTemp = large.encryptToTempFile(new ByteArrayInputStream(plain), password);

        byte[] coverBytes = bufferedImageToPngBytes(img);
        var metadata = new StegoMetadataDTO(2, false, true, aes.generateKey(password), "big.bin");

        // Embed via streaming
        try (var encIn = Files.newInputStream(encTemp.path())) {
            var stegoBytes = lsb.encodeStream(coverBytes, encIn, encTemp.length(), metadata);

            // Decode (simulate decode path)
            var stegoImage = bytesToImage(stegoBytes);
            var extractedMeta = lsb.extractMetadata(stegoImage);
            assertEquals(metadata.lsbDepth(), extractedMeta.lsbDepth());

            var encryptedPayload = lsb.decode(stegoImage, extractedMeta.lsbDepth());
            var decrypted = aes.decryptFile(encryptedPayload, password);
            assertArrayEquals(plain, decrypted);
        } finally {
            Files.deleteIfExists(encTemp.path());
        }
    }
}