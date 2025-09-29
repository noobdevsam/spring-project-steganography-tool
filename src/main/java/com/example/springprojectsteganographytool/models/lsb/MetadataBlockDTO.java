package com.example.springprojectsteganographytool.models.lsb;

import java.awt.image.BufferedImage;

public record MetadataBlockDTO(
        BufferedImage working,
        byte[] metaBlock,
        int metaPixelCount,
        long payloadCapacityBytes
) {
}
