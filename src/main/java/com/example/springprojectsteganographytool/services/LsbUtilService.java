package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.exceptions.data.MessageTooLargeException;
import com.example.springprojectsteganographytool.exceptions.file.InvalidImageFormatException;
import com.example.springprojectsteganographytool.exceptions.lsb.InvalidLsbDepthException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbDecodingException;
import com.example.springprojectsteganographytool.exceptions.lsb.LsbEncodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataDecodingException;
import com.example.springprojectsteganographytool.exceptions.metadata.MetadataNotFoundException;
import com.example.springprojectsteganographytool.models.StegoMetadataDTO;

import java.awt.image.BufferedImage;
import java.io.InputStream;

public interface LsbUtilService {

    byte[] encode(
            byte[] imageBytes,
            byte[] payloadBytes,
            StegoMetadataDTO metadata
    ) throws MessageTooLargeException, LsbEncodingException;

    // Streaming encode: payload length known, data provided as stream
    byte[] encodeStream(
            byte[] imageBytes,
            InputStream payloadStream,
            long payloadLength,
            StegoMetadataDTO metadata
    ) throws MessageTooLargeException, LsbEncodingException;

    //New: BufferedImage-based APIs (Phase 3)
    StegoMetadataDTO extractMetadata(BufferedImage stegoImage) throws
            InvalidImageFormatException, MessageTooLargeException,
            MetadataNotFoundException, MetadataDecodingException;

    byte[] decode(
            BufferedImage stegoImage,
            Integer lsbDepth
    ) throws InvalidLsbDepthException, LsbDecodingException;
}