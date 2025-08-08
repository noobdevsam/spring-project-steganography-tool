package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.models.StegoMetadataDTO;

public interface LsbUtilService {

    byte[] encodeMessage(
            byte[] imageBytes,
            byte[] messageBytes,
            StegoMetadataDTO metadata
    ) throws Exception;

    byte[] decodeMessage(
            byte[] stegoImageBytes,
            int lsbDepth
    ) throws Exception;

    byte[] encodeFile(
            byte[] imageBytes,
            byte[] fileBytes,
            StegoMetadataDTO metadata
    ) throws Exception;

    byte[] decodeFile(
            byte[] stegoImageBytes,
            int lsbDepth
    ) throws Exception;

    StegoMetadataDTO extractMetadata(
            byte[] stegoImageBytes
    ) throws Exception;

}
