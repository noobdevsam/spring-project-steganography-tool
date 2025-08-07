package com.example.springprojectsteganographytool.mappers;

import com.example.springprojectsteganographytool.documents.StegoData;
import com.example.springprojectsteganographytool.models.StegoDecodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoDownloadDTO;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoMetadataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StegoDataMapper {

    StegoEncodeResponseDTO StegoDataToEncodeResponseDTO(StegoData stegoData);

    StegoMetadataDTO StegoDataToMetadataDTO(StegoData stegoData);

    @Mapping(source = "embeddedFileName", target = "fileName")
    @Mapping(source = "embeddedFileBytes", target = "fileData")
    StegoDownloadDTO StegoDataToDownloadDTO(StegoData stegoData);

    StegoDecodeResponseDTO StegoDataToDecodeResponseDTO(StegoData stegoData);

}
