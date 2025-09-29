package com.example.springprojectsteganographytool.models.lsb;

import java.awt.image.BufferedImage;

public record HeaderInfoDTO(
        BufferedImage image,
        int headerPixels,
        int metaLength
) {
}
