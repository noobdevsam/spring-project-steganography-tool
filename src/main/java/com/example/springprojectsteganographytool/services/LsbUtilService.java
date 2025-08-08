package com.example.springprojectsteganographytool.services;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface LsbUtilService {

    long getMaxPayloadSizeBytes(
            BufferedImage image,
            int lsbDepth
    );

    BufferedImage embedPayload(
            BufferedImage image,
            byte[] payload,
            int lsbDepth
    );

    byte[] extractPayload(
            BufferedImage image,
            int payloadLength,
            int lsbDepth
    );

    byte[] bufferedImageToBytes(
            BufferedImage image,
            String format
    ) throws IOException;

    BufferedImage bytesToBufferedImage(
            byte[] bytes
    ) throws IOException;

    BufferedImage embedWithLengthHeader(
            BufferedImage coverImage,
            byte[] payload,
            int lsbDepth
    );

    byte[] extractWithLengthHeader(
            BufferedImage stegoImage,
            int lsbDepth
    );
}
