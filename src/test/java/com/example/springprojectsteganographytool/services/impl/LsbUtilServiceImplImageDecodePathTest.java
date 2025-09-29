package com.example.springprojectsteganographytool.services.impl;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LsbUtilServiceImplImageDecodePathTest {

    private final LsbUtilServiceImpl service = new LsbUtilServiceImpl();

    @Test
    void extractMetadataOnNotStegoImageFails() {
        var img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        assertThrows(RuntimeException.class, () -> service.extractMetadata(img));
    }


}